package koza.licensemanagementservice.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VendorStatsResponse {
    private long totalSoftware;
    private long totalLicenses;
    private long activeSessions;
}
