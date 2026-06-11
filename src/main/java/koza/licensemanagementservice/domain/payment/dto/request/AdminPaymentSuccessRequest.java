package koza.licensemanagementservice.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminPaymentSuccessRequest(
        @NotBlank(message = "사유는 필수입니다.")
        @Size(max = 255, message = "사유는 최대 255자입니다.")
        String reason
) {
}
