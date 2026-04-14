package koza.licensemanagementservice.domain.license.log.dto;

import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LicenseStatusChangedEvent {
    private Long targetId;
    private Long operatorId;
    private LicenseStatus beforeStatus;
    private LicenseStatus afterStatus;
    private String reason;
}
