package koza.licensemanagementservice.domain.license.log.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LicenseExtendEvent {
    private Long operatorId;
    private Long licenseId;
    private LocalDateTime beforeExpiredAt;
    private LocalDateTime afterExpiredAt;
    private Long periodMs;
}
