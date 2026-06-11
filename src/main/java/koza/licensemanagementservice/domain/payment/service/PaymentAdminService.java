package koza.licensemanagementservice.domain.payment.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.billing.toss.service.TossBillingService;
import koza.licensemanagementservice.domain.payment.dto.condition.PaymentAdminSearchCondition;
import koza.licensemanagementservice.domain.payment.dto.request.AdminPaymentSuccessRequest;
import koza.licensemanagementservice.domain.payment.dto.request.AdminPaymentFailRequest;
import koza.licensemanagementservice.domain.payment.dto.request.AdminPaymentRefundRequest;
import koza.licensemanagementservice.domain.payment.dto.response.AdminPaymentDetailResponse;
import koza.licensemanagementservice.domain.payment.dto.response.AdminPaymentSummaryResponse;
import koza.licensemanagementservice.domain.payment.entity.Payment;
import koza.licensemanagementservice.domain.payment.entity.PaymentStatus;
import koza.licensemanagementservice.domain.payment.event.PaymentAdminApprovedEvent;
import koza.licensemanagementservice.domain.payment.event.PaymentAdminFailedEvent;
import koza.licensemanagementservice.domain.payment.event.PaymentAdminRefundedEvent;
import koza.licensemanagementservice.domain.payment.repository.PaymentRepository;
import koza.licensemanagementservice.domain.subscription.entity.Subscription;
import koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus;
import koza.licensemanagementservice.domain.subscription.repository.SubscriptionRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.global.error.PaymentException;
import koza.licensemanagementservice.global.validation.ValidUserAuthorized;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentAdminService {
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PendingPaymentResolverService pendingPaymentResolverService;
    private final TossBillingService tossBillingService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<AdminPaymentSummaryResponse> getPayments(CustomUser user, PaymentAdminSearchCondition condition, Pageable pageable) {
        ValidUserAuthorized.validAdminAuthorized(user);
        return paymentRepository.searchPayments(condition, pageable);
    }

    @Transactional(readOnly = true)
    public AdminPaymentDetailResponse getPaymentDetail(CustomUser user, Long paymentId) {
        ValidUserAuthorized.validAdminAuthorized(user);
        Payment payment = paymentRepository.findByIdFetchAll(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return AdminPaymentDetailResponse.of(payment);
    }

    @Transactional(readOnly = true)
    public List<AdminPaymentDetailResponse> getPaymentsBySubscription(CustomUser user, Long subscriptionId) {
        ValidUserAuthorized.validAdminAuthorized(user);
        return paymentRepository.findBySubscriptionIdFetchAll(subscriptionId)
                .stream()
                .map(AdminPaymentDetailResponse::of)
                .toList();
    }

    @Transactional
    public void manualSuccess(CustomUser user, Long paymentId, AdminPaymentSuccessRequest request) {
        ValidUserAuthorized.validAdminAuthorized(user);
        Payment payment = paymentRepository.findByIdFetchAll(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.PENDING)
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING);

        Subscription subscription = payment.getSubscription();
        if (subscription == null || subscription.getStatus() == SubscriptionStatus.DELETED) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_RESOLVE);
        }

        payment.manualSuccess();
        if (subscription.getStatus() == SubscriptionStatus.PENDING) {
            subscription.start(subscription.getPlan().getPlanCode());
        } else {
            subscription.renewal();
        }

        String targetEmail = payment.getMember() != null ? payment.getMember().getEmail() : null;
        eventPublisher.publishEvent(new PaymentAdminApprovedEvent(
                paymentId, user.getId(), targetEmail, payment.getOrderId(), request.reason()));
    }

    @Transactional
    public void fail(CustomUser user, Long paymentId, AdminPaymentFailRequest request) {
        ValidUserAuthorized.validAdminAuthorized(user);
        Payment payment = paymentRepository.findByIdFetchAll(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.PENDING)
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING);

        payment.fail("ADMIN_MANUAL", request.reason());

        Subscription subscription = payment.getSubscription();
        if (subscription != null) {
            if (subscription.getStatus() == SubscriptionStatus.PENDING) {
                subscription.delete();
            } else {
                subscription.failedBilling();
                subscriptionRepository.saveAndFlush(subscription);
            }
        }
        paymentRepository.saveAndFlush(payment);

        String targetEmail = payment.getMember() != null ? payment.getMember().getEmail() : null;
        eventPublisher.publishEvent(new PaymentAdminFailedEvent(
                paymentId, user.getId(), targetEmail, payment.getOrderId(), request.reason()));
    }

    public void resolve(CustomUser user, Long paymentId) {
        ValidUserAuthorized.validAdminAuthorized(user);
        Payment payment = paymentRepository.findByIdFetchAll(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.PENDING)
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING);

        Subscription subscription = payment.getSubscription();
        if (subscription == null || subscription.getStatus() == SubscriptionStatus.DELETED) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_RESOLVE);
        }

        pendingPaymentResolverService.resolveByOrderId(payment.getOrderId());
    }

    public void refund(CustomUser user, Long paymentId, AdminPaymentRefundRequest request) {
        ValidUserAuthorized.validAdminAuthorized(user);
        Payment payment = paymentRepository.findByIdFetchAll(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.SUCCESS)
            throw new BusinessException(ErrorCode.PAYMENT_NOT_SUCCESS);

        // 결제 승인 시점으로부터 7일이 지났으면
        long daysBetween = ChronoUnit.DAYS.between(payment.getApprovedAt(), LocalDateTime.now());
        if (daysBetween >= 7)
            throw new BusinessException(ErrorCode.PAYMENT_REFUNDED_BELATED);

        try {
            tossBillingService.cancelPayment(payment.getPaymentKey(), request.reason());
        } catch (PaymentException pe) {
            throw pe;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PAYMENT_REFUND_FAILED);
        }

        payment.refund();
        paymentRepository.saveAndFlush(payment);

        Subscription subscription = payment.getSubscription();
        if (subscription != null) {
            SubscriptionStatus status = subscription.getStatus();
            if (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.PAST_DUE || status == SubscriptionStatus.CANCELLED) {
                subscription.refund();
                subscriptionRepository.saveAndFlush(subscription);
            }
        }

        String targetEmail = payment.getMember() != null ? payment.getMember().getEmail() : null;
        eventPublisher.publishEvent(new PaymentAdminRefundedEvent(
                paymentId, user.getId(), targetEmail, payment.getPaymentKey(), payment.getAmount(), request.reason()));
        System.out.println("환불 이벤트 발행");
    }
}
