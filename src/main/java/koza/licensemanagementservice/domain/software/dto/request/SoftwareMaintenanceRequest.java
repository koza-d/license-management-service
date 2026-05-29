package koza.licensemanagementservice.domain.software.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SoftwareMaintenanceRequest {
    @NotNull(message = "유지보수 종료 시점은 필수입니다.")
    @Future(message = "유지보수 종료 시점은 미래여야 합니다.")
    private LocalDateTime untilAt;

    @NotBlank(message = "사유는 필수입니다.")
    @Size(max = 500, message = "사유는 최대 500자입니다.")
    private String reason;
}
