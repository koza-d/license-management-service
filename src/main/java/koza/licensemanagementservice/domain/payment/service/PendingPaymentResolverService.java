package koza.licensemanagementservice.domain.payment.service;

import koza.licensemanagementservice.domain.billing.toss.dto.TossPaymentResponse;
import koza.licensemanagementservice.domain.billing.toss.service.TossBillingService;
import koza.licensemanagementservice.domain.payment.dto.PaymentPendingResult;
import koza.licensemanagementservice.domain.payment.entity.Payment;
import koza.licensemanagementservice.domain.payment.entity.PaymentStatus;
import koza.licensemanagementservice.domain.payment.repository.PaymentRepository;
import koza.licensemanagementservice.domain.subscription.entity.Subscription;
import koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus;
import koza.licensemanagementservice.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PendingPaymentResolverService {
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TossBillingService tossBillingService;

    public PaymentPendingResult resolveByOrderId(String orderId) {
        Optional<Payment> paymentOpt = paymentRepository.findByOrderIdWithSubscription(orderId);
        if (paymentOpt.isEmpty()) return null;

        Payment payment = paymentOpt.get();
        if (payment.getStatus() != PaymentStatus.PENDING) return null;

        var paymentResult = tossBillingService.getPaymentByOrderId(orderId);
        TossPaymentResponse response = paymentResult.getFirst();
        String rawResponse = paymentResult.getSecond();

        Subscription subscription = payment.getSubscription();

        switch (response.getStatus()) {
            case "DONE" -> {
                payment.success(response, rawResponse);
                if (subscription.getStatus() == SubscriptionStatus.PENDING) {
                    // 구독 시작 성공
                    subscription.start(subscription.getPlan().getPlanCode());
                } else {
                    // 구독 연장 결제 성공
                    subscription.renewal();
                }
                paymentRepository.saveAndFlush(payment);
                subscriptionRepository.saveAndFlush(subscription);
                log.info("PENDING 결제 해소 -> SUCCESS | orderId={}", orderId);
                return PaymentPendingResult.SUCCEED;
            }
            case "ABORTED", "CANCELED" -> {
                payment.fail(response.getStatus(), "토스 결제 " + response.getStatus());
                if (subscription.getStatus() == SubscriptionStatus.PENDING) {
                    // 구독 시작 결제 실패
                    subscriptionRepository.delete(subscription);
                } else {
                    // 구독 연장 결제 실패
                    subscription.failedBilling();
                    subscriptionRepository.saveAndFlush(subscription);
                }
                paymentRepository.saveAndFlush(payment);
                log.info("PENDING 결제 해소 -> FAILED | orderId={} | reason={}", orderId, response.getStatus());
                return PaymentPendingResult.FAILED;
            }
            default -> {
                log.info("PENDING 결제 미확정 상태 유지 | orderId={} | tossStatus={}", orderId, response.getStatus());
                return PaymentPendingResult.UNCONFIRMED;
            }
        }
    }
}
