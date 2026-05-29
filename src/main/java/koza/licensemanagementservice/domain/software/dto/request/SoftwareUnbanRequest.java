package koza.licensemanagementservice.domain.software.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SoftwareUnbanRequest {
    @NotBlank(message = "사유는 필수입니다.")
    @Size(max = 500, message = "사유는 최대 500자입니다.")
    private final String reason;
}
