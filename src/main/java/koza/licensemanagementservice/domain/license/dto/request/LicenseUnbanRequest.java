package koza.licensemanagementservice.domain.license.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LicenseUnbanRequest {
    private final String reason;
}
