package koza.licensemanagementservice.domain.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminSubscriptionCancelRequest(
        @NotBlank(message = "사유는 필수입니다.")
        @Size(max = 500, message = "사유는 최대 500자입니다.")
        String reason
) {
}
