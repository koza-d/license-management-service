package koza.licensemanagementservice.domain.subscription.repository;

import koza.licensemanagementservice.domain.subscription.dto.AdminSubscriptionSummaryResponse;
import koza.licensemanagementservice.domain.subscription.dto.condition.SubscriptionAdminSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubscriptionRepositoryCustom {
    Page<AdminSubscriptionSummaryResponse> searchSubscriptions(SubscriptionAdminSearchCondition condition, Pageable pageable);
}
