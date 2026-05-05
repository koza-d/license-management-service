package koza.licensemanagementservice.dashboard.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class SoftwareUsageResponse {
    private final Long softwareId;
    private final String softwareName;
    private final long totalMinutes;
    private final long activeSessionCount;
    private final long licenseCount;

    @QueryProjection
    public SoftwareUsageResponse(Long softwareId, String softwareName, Long totalMinutes,
                                 Long activeSessionCount, Long licenseCount) {
        this.softwareId = softwareId;
        this.softwareName = softwareName;
        this.totalMinutes = totalMinutes != null ? totalMinutes : 0L;
        this.activeSessionCount = activeSessionCount != null ? activeSessionCount : 0L;
        this.licenseCount = licenseCount != null ? licenseCount : 0L;
    }
}
