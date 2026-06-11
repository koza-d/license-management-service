package koza.licensemanagementservice.domain.subscription.dto;

import com.querydsl.core.annotations.QueryProjection;
import koza.licensemanagementservice.domain.plan.entity.PlanCode;
import koza.licensemanagementservice.domain.subscription.entity.BillingCycle;
import koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus;

import java.time.LocalDateTime;

public record AdminSubscriptionSummaryResponse(Long subscriptionId, String email, String nickname, PlanCode planCode,
                                               BillingCycle billingCycle, SubscriptionStatus status,
                                               LocalDateTime currentPeriodStart, LocalDateTime currentPeriodEnd,
                                               LocalDateTime startedAt) {
    @QueryProjection
    public AdminSubscriptionSummaryResponse {
    }
}
