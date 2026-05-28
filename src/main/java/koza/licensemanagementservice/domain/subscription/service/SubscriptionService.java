package koza.licensemanagementservice.domain.subscription.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.billing.toss.dto.CardIssuer;
import koza.licensemanagementservice.domain.billing.toss.dto.TossPaymentRequest;
import koza.licensemanagementservice.domain.billing.toss.dto.TossPaymentResponse;
import koza.licensemanagementservice.domain.billing.toss.service.TossBillingService;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.payment.dto.PaymentPendingResult;
import koza.licensemanagementservice.domain.payment.entity.Payment;
import koza.licensemanagementservice.domain.payment.entity.PaymentStatus;
import koza.licensemanagementservice.domain.payment.repository.PaymentRepository;
import koza.licensemanagementservice.domain.payment.service.PendingPaymentResolverService;
import koza.licensemanagementservice.domain.paymentmethod.entity.PaymentMethod;
import koza.licensemanagementservice.domain.paymentmethod.repository.PaymentMethodRepository;
import koza.licensemanagementservice.domain.plan.entity.Plan;
import koza.licensemanagementservice.domain.plan.entity.PlanCode;
import koza.licensemanagementservice.domain.plan.repository.PlanRepository;
import koza.licensemanagementservice.domain.subscription.dto.SubscriptionResponse;
import koza.licensemanagementservice.domain.subscription.dto.SubscriptionStartRequest;
import koza.licensemanagementservice.domain.subscription.entity.BillingCycle;
import koza.licensemanagementservice.domain.subscription.entity.Subscription;
import koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus;
import koza.licensemanagementservice.domain.subscription.repository.SubscriptionRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.global.error.PaymentException;
import koza.licensemanagementservice.global.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    private final MemberRepository memberRepository;
    private final PlanRepository planRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    private final PendingPaymentResolverService paymentResolverService;
    private final TossBillingService tossBillingService;
    private final EncryptUtil encryptUtil;

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(CustomUser user) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Optional<Subscription> activeSubscriptionOpt = subscriptionRepository.findActiveSubscription(member.getId());
        return activeSubscriptionOpt.map(SubscriptionResponse::of).orElse(null);

    }

    public void start(CustomUser user, SubscriptionStartRequest request) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Plan plan = planRepository.findByPlanCode(request.getPlanCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        if (plan.getPlanCode() == PlanCode.FREE)
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        PaymentMethod defaultMethod = paymentMethodRepository.findByMemberIdAndIsDefaultIsTrueAndIsActiveIsTrue(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        // 기존 ACTIVE 구독있으면 실패
        subscriptionRepository.findActiveSubscription(member.getId())
                .ifPresent(s -> { throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_ACTIVE); });

        // 기존 PENDING 구독 확인 및 resolve
        Optional<Subscription> pendingSubOpt = subscriptionRepository.findByMemberIdAndStatus(member.getId(), SubscriptionStatus.PENDING);
        if (pendingSubOpt.isPresent()) {
            Subscription pendingSub = pendingSubOpt.get();
            // 해당 구독 건의 완결되지 못한 결제 조회
            Optional<Payment> pendingPayment = paymentRepository.findBySubscriptionIdAndStatus(pendingSub.getId(), PaymentStatus.PENDING);
            if (pendingPayment.isPresent()) {
                // 완결되지 못한 결제 있음 -> PG 결제결과 조회 후 완결
                PaymentPendingResult result = paymentResolverService.resolveByOrderId(pendingPayment.get().getOrderId());

                if (result == PaymentPendingResult.SUCCEED)
                    throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_ACTIVE);

                // 여전히 완결되지 못함
                if (result == PaymentPendingResult.UNCONFIRMED)
                    throw new BusinessException(ErrorCode.SUBSCRIPTION_PAYMENT_PENDING);

            } else {
                pendingSub.delete();
            }
        }

        // 새 구독 생성
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime periodEnd = getPeriodEnd(request.getBillingCycle(), startedAt);

        Subscription subscription = subscriptionRepository.saveAndFlush(Subscription.builder()
                .member(member)
                .plan(plan)
                .paymentMethod(defaultMethod)
                .status(SubscriptionStatus.PENDING)
                .startedAt(startedAt)
                .billingCycle(request.getBillingCycle())
                .currentPeriodStart(startedAt)
                .currentPeriodEnd(periodEnd)
                .build());

        String billingKey = encryptUtil.decrypt(defaultMethod.getBillingKey());
        String orderId = createOrderId();
        String orderName = plan.getName() + " 플랜 구독";

        try {
            chargeToss(member, plan, subscription, billingKey, orderId, orderName, request.getBillingCycle());
        } catch (PaymentException e) {
            log.info("memberId={} | memberEmail={} | 구독 실패 | OrderId={} | 사유={}", member.getId(), member.getEmail(), orderId, e.getMessage());
            subscription.delete();
            subscriptionRepository.saveAndFlush(subscription);
            throw e;
        } catch (Exception e) {
            log.warn("memberId={} | memberEmail={} | 결제 응답 미수신 | orderId={}", member.getId(), member.getEmail(), orderId);
            throw new BusinessException(ErrorCode.SUBSCRIPTION_PAYMENT_PENDING);
        }

        subscription.start(plan.getPlanCode());
        subscriptionRepository.saveAndFlush(subscription);
        log.info("memberId={} | memberEmail={} | 구독 성공 | SubscriptionId={} | OrderId={}", member.getId(), member.getEmail(), subscription.getId(), orderId);
    }

    // 구독 갱신 (사용자)
    public void renewal(CustomUser user, Long subscriptionId) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Subscription subscription = subscriptionRepository.findByIdFetchAll(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        if (subscription.getStatus() == SubscriptionStatus.DELETED)
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        Plan plan = subscription.getPlan();
        PaymentMethod paymentMethod = subscription.getPaymentMethod();
        if (!paymentMethod.isActive())
            throw new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_ACTIVE);

        // 구독 종료 7일전부터 수동 갱신 가능
        LocalDateTime todayStartAt = LocalDate.now().atStartOfDay();
        LocalDateTime periodEndBeforeDays = subscription.getCurrentPeriodEnd().minusDays(7);
        if (!todayStartAt.isAfter(periodEndBeforeDays))
            throw new BusinessException(ErrorCode.SUBSCRIPTION_RENEWAL_EARLY);

        Optional<Payment> pendingPaymentOpt = paymentRepository.findBySubscriptionIdAndStatus(subscriptionId, PaymentStatus.PENDING);
        if (pendingPaymentOpt.isPresent()) {
            Payment pendingPayment = pendingPaymentOpt.get();
            PaymentPendingResult result = paymentResolverService.resolveByOrderId(pendingPayment.getOrderId());

            // 기존 결제건 성공했을 시
            if (result == PaymentPendingResult.SUCCEED)
                throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_ACTIVE);

            // 기존 결제건 완결 x
            if (result == PaymentPendingResult.UNCONFIRMED)
                throw new BusinessException(ErrorCode.SUBSCRIPTION_PAYMENT_PENDING);
        }

        String billingKey = encryptUtil.decrypt(paymentMethod.getBillingKey());
        String orderId = createOrderId();
        String orderName = plan.getName() + " 플랜 구독 갱신";
        try {
            chargeToss(member, plan, subscription, billingKey, orderId, orderName, subscription.getBillingCycle());
        } catch (PaymentException pe) {
            log.info("memberId={} | memberEmail={} | 구독 갱신 실패 | 사유={}", member.getId(), member.getEmail(), pe.getMessage());
            subscription.failedBilling();
            subscriptionRepository.saveAndFlush(subscription);
            throw pe;
        } catch (Exception e) {
            log.warn("memberId={} | memberEmail={} | 결제 응답 미수신 | orderId={}", member.getId(), member.getEmail(), orderId);
            throw new BusinessException(ErrorCode.SUBSCRIPTION_PAYMENT_PENDING);
        }
        subscription.renewal();
        subscriptionRepository.saveAndFlush(subscription);
        log.info("memberId={} | memberEmail={} | 구독 갱신 성공 | SubscriptionId={} | OrderId={}", member.getId(), member.getEmail(), subscription.getId(), orderId);
    }

    @Transactional
    public void cancel(CustomUser user, Long subscriptionId) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        if (!subscription.getMember().getId().equals(member.getId())
                || subscription.getStatus() == SubscriptionStatus.DELETED)
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        // 만료됐거나 취소된 구독인 경우
        if (subscription.getStatus() == SubscriptionStatus.EXPIRED || subscription.getStatus() == SubscriptionStatus.CANCELLED)
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        subscription.cancel();

        // 결제 예정일이 지난 경우 만료처리
        if (subscription.getStatus() == SubscriptionStatus.PAST_DUE)
            subscription.expire();
    }

    @Transactional
    public void resume(CustomUser user, Long subscriptionId) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        // 요청자의 구독이 아닌 경우
        if (!subscription.getMember().getId().equals(member.getId()))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        // 취소된 구독이 아니면
        if (subscription.getStatus() != SubscriptionStatus.CANCELLED)
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        subscription.resume();
    }

    @Transactional
    public void changePaymentMethod(CustomUser user, Long subscriptionId, Long paymentMethodId) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        if (subscription.getStatus() == SubscriptionStatus.DELETED)
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        // 요청한 유저의 구독이 아닌 경우
        if (!subscription.getMember().getId().equals(member.getId()))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        // 사용할 수 없는 결제수단
        if (!paymentMethod.isActive())
            throw new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_ACTIVE);

        // 요청한 유저의 결제수단이 아닌 경우
        if (!paymentMethod.getMember().getId().equals(member.getId()))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        subscription.changePaymentMethod(paymentMethod);
    }

    // 구독 결제 메서드
    private void chargeToss(Member member, Plan plan, Subscription subscription, String billingKey, String orderId, String orderName, BillingCycle billingCycle) {
        TossPaymentRequest tossPaymentRequest = TossPaymentRequest.builder()
                .customerKey(member.getPaymentKey())
                .orderId(orderId)
                .orderName(orderName)
                .amount(getPlanPrice(billingCycle, plan))
                .build();

        PaymentMethod paymentMethod = subscription.getPaymentMethod();
        Payment payment = Payment.builder()
                .subscription(subscription)
                .member(member)
                .orderId(orderId)
                .orderName(orderName)
                .cardType(paymentMethod.getCardType())
                .cardOwnerType(paymentMethod.getCardOwnerType())
                .cardIssuerCode(paymentMethod.getCardIssuerCode())
                .cardCompany(CardIssuer.getKoreanName(paymentMethod.getCardIssuerCode()))
                .cardNumberMasked(paymentMethod.getCardNumberMasked())
                .amount((long) tossPaymentRequest.getAmount())
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.saveAndFlush(payment);
        String rawResponse = null;
        TossPaymentResponse tossPaymentResponse = null;
        try {
            var chargeResult = tossBillingService.charge(billingKey, tossPaymentRequest);
            tossPaymentResponse = chargeResult.getFirst();
            rawResponse = chargeResult.getSecond();
        } catch (PaymentException pe) {
            payment.fail(pe.getCode(), pe.getMessage());
            paymentRepository.saveAndFlush(payment);
            throw pe;
        } catch (Exception e) {
            log.error("subscriptionId={}, orderId={} | 결제도중 예상치 못한 에러 발생 | 사유={}", subscription.getId(), orderId, e.getMessage());
            throw e;
        }

        payment.success(tossPaymentResponse, rawResponse);
        paymentRepository.saveAndFlush(payment);
    }

    // 구독 갱신 (스케줄러)
    public Pair<Long, Long> scheduleRenewal() {
        long succeed = 0;
        long failed = 0;
        List<Subscription> needRenewalSubscriptions = subscriptionRepository.findNeedRenewalSubscriptions(LocalDate.now().atStartOfDay());
        for (Subscription subscription : needRenewalSubscriptions) {
            try {
                Member member = subscription.getMember();
                Plan plan = subscription.getPlan();
                PaymentMethod paymentMethod = subscription.getPaymentMethod();

                Optional<Payment> pendingPaymentOpt = paymentRepository.findBySubscriptionIdAndStatus(subscription.getId(), PaymentStatus.PENDING);
                if (pendingPaymentOpt.isPresent()) {
                    Payment pendingPayment = pendingPaymentOpt.get();
                    PaymentPendingResult result = paymentResolverService.resolveByOrderId(pendingPayment.getOrderId());

                    // 기존 결제건 성공했을 시
                    if (result == PaymentPendingResult.SUCCEED) {
                        succeed++;
                        continue;
                    }

                    // 기존 결제건 완결 x
                    if (result == PaymentPendingResult.UNCONFIRMED) {
                        failed++;
                        continue;
                    }
                }

                String billingKey = encryptUtil.decrypt(paymentMethod.getBillingKey());
                String orderId = createOrderId();
                String orderName = plan.getName() + " 플랜 구독 갱신";
                try {
                    chargeToss(member, plan, subscription, billingKey, orderId, orderName, subscription.getBillingCycle());
                } catch (Exception e) {
                    log.info("memberId={} | memberEmail={} | 구독 갱신 실패 | 사유={}", member.getId(), member.getEmail(), e.getMessage());
                    failed++;
                    subscription.failedBilling();
                    subscriptionRepository.saveAndFlush(subscription);
                    continue;
                }
                subscription.renewal();
                subscriptionRepository.saveAndFlush(subscription);
                log.info("memberId={} | memberEmail={} | 구독 갱신 성공 | SubscriptionId={} | OrderId={}", member.getId(), member.getEmail(), subscription.getId(), orderId);
                succeed++;
            } catch (Exception e) {
                log.warn("구독 갱신 중 예상치 못한 예외가 발생했습니다. 사유={}", e.getMessage());
                subscription.failedBilling();
                subscriptionRepository.saveAndFlush(subscription);
                failed++;
            }
        }
        return Pair.of(succeed, failed);
    }

    @Transactional
    public int scheduleExpired() {
        List<Subscription> expiredTarget = subscriptionRepository.findExpiredTarget(LocalDate.now().atStartOfDay());
        for (Subscription subscription : expiredTarget) {
            subscription.expire();
        }
        return expiredTarget.size();
    }

    private String createOrderId() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6);
    }

    private LocalDateTime getPeriodEnd(BillingCycle billingCycle, LocalDateTime periodStart) {
        switch (billingCycle) {
            case MONTHLY -> {
                return periodStart.plusMonths(1);
            }
            case YEARLY -> {
                return periodStart.plusYears(1);
            }
            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private int getPlanPrice(BillingCycle billingCycle, Plan plan) {
        switch (billingCycle) {
            case MONTHLY -> {
                return plan.getMonthlyPrice();
            }
            case YEARLY -> {
                return plan.getYearlyPrice();
            }
            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

}
