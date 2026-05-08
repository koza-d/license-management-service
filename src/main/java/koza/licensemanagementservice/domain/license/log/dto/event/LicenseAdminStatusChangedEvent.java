package koza.licensemanagementservice.domain.license.log.dto.event;

import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LicenseAdminStatusChangedEvent {
    private Long targetId;
    private Long operatorId;
    private LicenseStatus beforeStatus;
    private LicenseStatus afterStatus;
    private LocalDateTime until;
    private String reason;

    public LicenseAdminStatusChangedEvent(Long targetId, Long operatorId,
                                          LicenseStatus beforeStatus, LicenseStatus afterStatus,
                                          String reason) {
        this.targetId = targetId;
        this.operatorId = operatorId;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.until = null;
        this.reason = reason;
    }
}
