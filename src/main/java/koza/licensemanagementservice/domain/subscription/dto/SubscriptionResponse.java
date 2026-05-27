package koza.licensemanagementservice.domain.subscription.dto;

import koza.licensemanagementservice.domain.paymentmethod.dto.PaymentMethodResponse;
import koza.licensemanagementservice.domain.plan.entity.Plan;
import koza.licensemanagementservice.domain.subscription.entity.BillingCycle;
import koza.licensemanagementservice.domain.subscription.entity.Subscription;
import koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubscriptionResponse {
    private final Long id;
    private final Plan plan;
    private final PaymentMethodResponse paymentMethod;
    private final SubscriptionStatus status;
    private final LocalDateTime startedAt;
    private final BillingCycle billingCycle;
    private final LocalDateTime currentPeriodStart;
    private final LocalDateTime currentPeriodEnd;
    private final LocalDateTime nextBillingAt;
    private final LocalDateTime gracePeriodEnd;
    private final LocalDateTime cancelledAt;

    public static SubscriptionResponse of(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .plan(subscription.getPlan())
                .paymentMethod(PaymentMethodResponse.of(subscription.getPaymentMethod()))
                .status(subscription.getStatus())
                .startedAt(subscription.getStartedAt())
                .billingCycle(subscription.getBillingCycle())
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .nextBillingAt(subscription.getNextBillingAt())
                .gracePeriodEnd(subscription.getGracePeriodEnd())
                .cancelledAt(subscription.getCancelledAt())
                .build();
    }
}
