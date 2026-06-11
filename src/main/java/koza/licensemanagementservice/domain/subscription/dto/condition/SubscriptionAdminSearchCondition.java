package koza.licensemanagementservice.domain.subscription.dto.condition;

import koza.licensemanagementservice.domain.plan.entity.PlanCode;
import koza.licensemanagementservice.domain.subscription.entity.BillingCycle;
import koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

/**
 * @param statusFilters null 또는 empty 전달 시 DELETED 제외하고 조회
 */
public record SubscriptionAdminSearchCondition(SubscriptionAdminSearchTarget target, String search,
                                               List<SubscriptionStatus> statusFilters, PlanCode planCode,
                                               BillingCycle billingCycle, LocalDate from, LocalDate to) {
}
