package koza.licensemanagementservice.domain.software.log.dto;

import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminSoftwareStatusChangedEvent {
    private Long targetSoftwareId;
    private Long operatorId;
    private SoftwareStatus beforeStatus;
    private SoftwareStatus afterStatus;
    private LocalDateTime until;
    private String reason;

    public AdminSoftwareStatusChangedEvent(Long targetSoftwareId, Long operatorId, SoftwareStatus beforeStatus, SoftwareStatus afterStatus, String reason) {
        this.targetSoftwareId = targetSoftwareId;
        this.operatorId = operatorId;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.until = null;
        this.reason = reason;
    }
}
