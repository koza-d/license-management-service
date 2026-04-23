package koza.licensemanagementservice.domain.software.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SoftwareMaintenanceRequest {
    private int untilDays;
    private String reason;
}
