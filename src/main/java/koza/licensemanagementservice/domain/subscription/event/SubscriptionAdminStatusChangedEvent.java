package koza.licensemanagementservice.domain.subscription.event;

import koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubscriptionAdminStatusChangedEvent {
    private Long subscriptionId;
    private Long operatorId;
    private String targetEmail;
    private SubscriptionStatus beforeStatus;
    private SubscriptionStatus afterStatus;
    private String reason;
}
