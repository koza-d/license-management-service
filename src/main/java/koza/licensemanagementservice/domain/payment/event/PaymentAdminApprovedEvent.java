package koza.licensemanagementservice.domain.payment.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentAdminApprovedEvent {
    private Long paymentId;
    private Long operatorId;
    private String targetEmail;
    private String orderId;
    private String reason;
}
