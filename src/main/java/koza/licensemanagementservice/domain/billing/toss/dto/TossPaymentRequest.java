package koza.licensemanagementservice.domain.billing.toss.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossPaymentRequest {
    private String customerKey;
    private String orderId;
    private String orderName;
    private int amount;
}
