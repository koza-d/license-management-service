package koza.licensemanagementservice.domain.payment.dto;

import koza.licensemanagementservice.domain.payment.entity.Payment;
import koza.licensemanagementservice.domain.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentSummaryResponse {
    private final Long paymentId;
    private final String orderName;
    private final PaymentStatus status;
    private final Long amount;
    private final LocalDateTime createAt;
    public static PaymentSummaryResponse of(Payment payment) {
        return PaymentSummaryResponse.builder()
                .paymentId(payment.getId())
                .orderName(payment.getOrderName())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .createAt(payment.getCreateAt())
                .build();
    }
}
