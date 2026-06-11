package koza.licensemanagementservice.domain.payment.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import koza.licensemanagementservice.domain.payment.entity.PaymentStatus;

import java.time.LocalDateTime;

public record AdminPaymentSummaryResponse(
        Long paymentId,
        String email,
        String nickname,
        String orderId,
        String orderName,
        Long amount,
        PaymentStatus status,
        LocalDateTime approvedAt
) {
    @QueryProjection
    public AdminPaymentSummaryResponse {
    }
}
