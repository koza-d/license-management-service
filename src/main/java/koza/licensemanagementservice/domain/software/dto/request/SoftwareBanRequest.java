package koza.licensemanagementservice.domain.software.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SoftwareBanRequest {
    @Min(value = 1, message = "정지 일수는 최소 1일입니다.")
    @Max(value = 365, message = "정지 일수는 최대 365일입니다.")
    private final int untilDays;

    @NotBlank(message = "사유는 필수입니다.")
    @Size(max = 500, message = "사유는 최대 500자입니다.")
    private final String reason;
}
