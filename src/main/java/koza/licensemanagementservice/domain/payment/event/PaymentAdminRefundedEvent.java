package koza.licensemanagementservice.domain.payment.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentAdminRefundedEvent {
    private Long paymentId;
    private Long operatorId;
    private String targetEmail;
    private String paymentKey;
    private Long amount;
    private String reason;
}
