package koza.licensemanagementservice.domain.subscription.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.subscription.dto.AdminSubscriptionDetailResponse;
import koza.licensemanagementservice.domain.subscription.dto.AdminSubscriptionSummaryResponse;
import koza.licensemanagementservice.domain.subscription.dto.condition.SubscriptionAdminSearchCondition;
import koza.licensemanagementservice.domain.subscription.dto.request.AdminSubscriptionCancelRequest;
import koza.licensemanagementservice.domain.subscription.entity.Subscription;
import koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus;
import koza.licensemanagementservice.domain.subscription.event.SubscriptionAdminStatusChangedEvent;
import koza.licensemanagementservice.domain.subscription.repository.SubscriptionRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.global.validation.ValidUserAuthorized;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionAdminService {
    private final SubscriptionRepository subscriptionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<AdminSubscriptionSummaryResponse> getSubscriptions(CustomUser user, SubscriptionAdminSearchCondition condition, Pageable pageable) {
        ValidUserAuthorized.validAdminAuthorized(user);
        return subscriptionRepository.searchSubscriptions(condition, pageable);
    }

    @Transactional(readOnly = true)
    public AdminSubscriptionDetailResponse getSubscriptionDetail(CustomUser user, Long subscriptionId) {
        ValidUserAuthorized.validAdminAuthorized(user);
        Subscription subscription = subscriptionRepository.findByIdFetchAll(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
        return AdminSubscriptionDetailResponse.of(subscription);
    }

    @Transactional
    public void cancel(CustomUser user, Long subscriptionId, AdminSubscriptionCancelRequest request) {
        ValidUserAuthorized.validAdminAuthorized(user);
        Subscription subscription = subscriptionRepository.findByIdFetchAll(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE && subscription.getStatus() != SubscriptionStatus.PAST_DUE)
            throw new BusinessException(ErrorCode.SUBSCRIPTION_NOT_ACTIVE_PAST_DUE);

        SubscriptionStatus beforeStatus = subscription.getStatus();
        subscription.cancel();

        eventPublisher.publishEvent(new SubscriptionAdminStatusChangedEvent(
                subscriptionId, user.getId(), subscription.getMember().getEmail(),
                beforeStatus, subscription.getStatus(), request.reason()));
    }

}
