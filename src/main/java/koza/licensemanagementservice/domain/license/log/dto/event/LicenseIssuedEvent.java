package koza.licensemanagementservice.domain.license.log.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class LicenseIssuedEvent {
    private Long licenseId;
    private Long operatorId;
    private Map<String, Object> snapshot;
    private LocalDateTime operatedAt;
}
