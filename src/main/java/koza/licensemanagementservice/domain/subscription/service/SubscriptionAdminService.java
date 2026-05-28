package koza.licensemanagementservice.domain.subscription.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.subscription.dto.AdminSubscriptionSummaryResponse;
import koza.licensemanagementservice.domain.subscription.dto.condition.SubscriptionAdminSearchCondition;
import koza.licensemanagementservice.domain.subscription.repository.SubscriptionRepository;
import koza.licensemanagementservice.global.validation.ValidUserAuthorized;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionAdminService {
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public Page<AdminSubscriptionSummaryResponse> getSubscriptions(CustomUser user, SubscriptionAdminSearchCondition condition, Pageable pageable) {
        ValidUserAuthorized.validAdminAuthorized(user);

        return subscriptionRepository.searchSubscriptions(condition, pageable);
    }
}
