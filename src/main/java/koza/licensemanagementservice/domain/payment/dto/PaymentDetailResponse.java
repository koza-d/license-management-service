package koza.licensemanagementservice.domain.payment.dto;

import koza.licensemanagementservice.domain.payment.entity.Payment;
import koza.licensemanagementservice.domain.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentDetailResponse {
    private final Long paymentId;
    private final String paymentKey;
    private final String orderId;
    private final String orderName;
    private final String cardType;
    private final String cardOwnerType;
    private final String cardCompany;
    private final String cardNumberMasked;
    private final String approveNo;
    private final LocalDateTime requestedAt;
    private final LocalDateTime approvedAt;
    private final String receiptUrl;
    private final PaymentStatus status;
    private final String failureCode;
    private final String failureReason;

    public static PaymentDetailResponse of(Payment payment) {
        return PaymentDetailResponse.builder()
                .paymentId(payment.getId())
                .paymentKey(payment.getPaymentKey())
                .orderId(payment.getOrderId())
                .orderName(payment.getOrderName())
                .cardType(payment.getCardType())
                .cardOwnerType(payment.getCardOwnerType())
                .cardCompany(payment.getCardCompany())
                .cardNumberMasked(payment.getCardNumberMasked())
                .approveNo(payment.getApproveNo())
                .requestedAt(payment.getRequestedAt())
                .approvedAt(payment.getApprovedAt())
                .receiptUrl(payment.getReceiptUrl())
                .status(payment.getStatus())
                .failureCode(payment.getFailureCode())
                .failureReason(payment.getFailureReason())
                .build();
    }
}
