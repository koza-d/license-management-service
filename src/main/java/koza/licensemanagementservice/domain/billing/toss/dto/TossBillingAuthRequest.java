package koza.licensemanagementservice.domain.billing.toss.dto;

import lombok.Getter;

@Getter
public class TossBillingAuthRequest {
    private String authKey;
    private String customerKey;
}
