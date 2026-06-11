package koza.licensemanagementservice.domain.subscription.dto;

import koza.licensemanagementservice.domain.paymentmethod.entity.PaymentMethod;
import koza.licensemanagementservice.domain.plan.entity.Plan;
import koza.licensemanagementservice.domain.subscription.entity.BillingCycle;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubscriptionStartResponse {
    private final Plan plan;
    private final PaymentMethod paymentMethod;
    private final BillingCycle billingCycle;
    private final LocalDateTime periodStart;
    private final LocalDateTime periodEnd;
    private final LocalDateTime nextBillingAt;
}
