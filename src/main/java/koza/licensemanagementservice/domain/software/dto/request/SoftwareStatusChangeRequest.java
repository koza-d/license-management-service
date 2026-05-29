package koza.licensemanagementservice.domain.software.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SoftwareStatusChangeRequest {
    @NotNull(message = "변경할 상태는 필수입니다.")
    private SoftwareStatus status;

    @NotBlank(message = "사유는 필수입니다.")
    @Size(max = 500, message = "사유는 최대 500자입니다.")
    private String reason;
}
