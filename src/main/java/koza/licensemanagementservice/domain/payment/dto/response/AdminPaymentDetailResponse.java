package koza.licensemanagementservice.domain.payment.dto.response;

import koza.licensemanagementservice.domain.payment.entity.Payment;
import koza.licensemanagementservice.domain.payment.entity.PaymentStatus;
import koza.licensemanagementservice.domain.plan.entity.PlanCode;
import koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus;

import java.time.LocalDateTime;

public record AdminPaymentDetailResponse(
        Long paymentId,
        String paymentKey,
        String orderId,
        String orderName,
        String cardType,
        String cardOwnerType,
        String cardCompany,
        String cardNumberMasked,
        String approveNo,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        String receiptUrl,
        Long amount,
        PaymentStatus status,
        String failureCode,
        String failureReason,
        LocalDateTime refundedAt,
        String memberEmail,
        String memberNickname,
        Long subscriptionId,
        SubscriptionStatus subscriptionStatus,
        PlanCode planCode
) {
    public static AdminPaymentDetailResponse of(Payment payment) {
        var member = payment.getMember();
        var subscription = payment.getSubscription();
        var plan = subscription != null ? subscription.getPlan() : null;
        return new AdminPaymentDetailResponse(
                payment.getId(),
                payment.getPaymentKey(),
                payment.getOrderId(),
                payment.getOrderName(),
                payment.getCardType(),
                payment.getCardOwnerType(),
                payment.getCardCompany(),
                payment.getCardNumberMasked(),
                payment.getApproveNo(),
                payment.getRequestedAt(),
                payment.getApprovedAt(),
                payment.getReceiptUrl(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getFailureCode(),
                payment.getFailureReason(),
                payment.getRefundedAt(),
                member != null ? member.getEmail() : null,
                member != null ? member.getNickname() : null,
                subscription != null ? subscription.getId() : null,
                subscription != null ? subscription.getStatus() : null,
                plan != null ? plan.getPlanCode() : null
        );
    }
}
