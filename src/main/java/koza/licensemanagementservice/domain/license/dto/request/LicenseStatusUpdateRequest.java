package koza.licensemanagementservice.domain.license.dto.request;

import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import lombok.Getter;

@Getter
public class LicenseStatusUpdateRequest {
    private LicenseStatus status;
    private String reason;
}
