package koza.licensemanagementservice.domain.software.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SoftwareMaintenanceRequest {
    private LocalDateTime untilAt;
    private String reason;
}
