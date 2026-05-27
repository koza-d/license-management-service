package koza.licensemanagementservice.domain.subscription.dto;

import koza.licensemanagementservice.domain.subscription.entity.BillingCycle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStartRequest {
    private String planCode;
    private BillingCycle billingCycle;
}
