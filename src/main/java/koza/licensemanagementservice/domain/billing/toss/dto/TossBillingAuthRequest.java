package koza.licensemanagementservice.domain.billing.toss.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TossBillingAuthRequest {
    @NotBlank(message = "인증 키는 필수입니다.")
    private String authKey;

    @NotBlank(message = "고객 키는 필수입니다.")
    private String customerKey;
}
