package koza.licensemanagementservice.domain.license.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LicenseUnbanRequest {
    @NotBlank(message = "사유는 필수입니다.")
    @Size(max = 100, message = "사유는 최대 100자입니다.")
    private final String reason;
}
