package koza.licensemanagementservice.domain.license.log.dto.event;

import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LicenseStatusChangedEvent {
    private Long targetId;
    private Long operatorId;
    private LicenseStatus beforeStatus;
    private LicenseStatus afterStatus;
    private String reason;
    private LocalDateTime operatedAt;
}
