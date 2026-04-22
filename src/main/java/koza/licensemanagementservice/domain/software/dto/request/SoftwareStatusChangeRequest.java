package koza.licensemanagementservice.domain.software.dto.request;

import jakarta.validation.constraints.NotNull;
import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SoftwareStatusChangeRequest {
    @NotNull(message = "변경할 상태는 필수입니다.")
    private SoftwareStatus status;

    private String reason;
}
