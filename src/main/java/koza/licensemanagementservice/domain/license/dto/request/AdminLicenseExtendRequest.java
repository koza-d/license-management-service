package koza.licensemanagementservice.domain.license.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminLicenseExtendRequest {
    @Min(value = 1, message = "연장 일수는 최소 1일입니다.")
    @Max(value = 365, message = "연장 일수는 최대 365일입니다.")
    private int days;
}
