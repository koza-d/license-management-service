package koza.licensemanagementservice.domain.subscription.dto;

import jakarta.validation.constraints.NotNull;
import koza.licensemanagementservice.domain.plan.entity.PlanCode;
import koza.licensemanagementservice.domain.subscription.entity.BillingCycle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStartRequest {
    @NotNull(message = "플랜 코드는 필수입니다.")
    private PlanCode planCode;

    @NotNull(message = "결제 주기는 필수입니다.")
    private BillingCycle billingCycle;
}
