package koza.licensemanagementservice.domain.license.log.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class LicenseModifiedEvent {
    private Long targetId;
    private Long operatorId;
    private Map<String, Object> before;
    private Map<String, Object> after;
}
