package koza.licensemanagementservice.domain.subscription.dto;

import koza.licensemanagementservice.domain.plan.entity.PlanCode;
import koza.licensemanagementservice.domain.subscription.entity.BillingCycle;
import koza.licensemanagementservice.domain.subscription.entity.Subscription;
import koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus;

import java.time.LocalDateTime;

public record AdminSubscriptionDetailResponse(
        Long id,
        String email,
        String nickname,
        PlanCode planCode,
        String planName,
        BillingCycle billingCycle,
        SubscriptionStatus status,
        LocalDateTime startedAt,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd,
        LocalDateTime nextBillingAt,
        LocalDateTime gracePeriodEnd,
        LocalDateTime cancelledAt,
        Long paymentMethodId,
        String paymentMethodLabel
) {
    public static AdminSubscriptionDetailResponse of(Subscription subscription) {
        var member = subscription.getMember();
        var plan = subscription.getPlan();
        var pm = subscription.getPaymentMethod();
        return new AdminSubscriptionDetailResponse(
                subscription.getId(),
                member.getEmail(),
                member.getNickname(),
                plan.getPlanCode(),
                plan.getName(),
                subscription.getBillingCycle(),
                subscription.getStatus(),
                subscription.getStartedAt(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.getNextBillingAt(),
                subscription.getGracePeriodEnd(),
                subscription.getCancelledAt(),
                pm != null ? pm.getId() : null,
                pm != null ? pm.getCardNumberMasked() : null
        );
    }
}
