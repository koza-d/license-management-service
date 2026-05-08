package koza.licensemanagementservice.domain.license.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LicenseBanRequest {
    private final int untilDays;
    private final String reason;
}
