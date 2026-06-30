package koza.licensemanagementservice.dashboard.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class SoftwareUsageResponse {
    private final Long softwareId;
    private final String softwareName;
    private final long totalSeconds;

    @QueryProjection
    public SoftwareUsageResponse(Long softwareId, String softwareName, Long totalSeconds) {
        this.softwareId = softwareId;
        this.softwareName = softwareName;
        this.totalSeconds = totalSeconds != null ? totalSeconds : 0L;
    }
}
