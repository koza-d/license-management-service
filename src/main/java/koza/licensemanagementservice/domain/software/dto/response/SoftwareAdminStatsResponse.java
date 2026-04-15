package koza.licensemanagementservice.domain.software.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class SoftwareAdminStatsResponse {
    private final Long totalUsageMs;
    private final Long usageSessionCount;
    private final Long avgUsageMs;

    @QueryProjection
    public SoftwareAdminStatsResponse(Long totalUsageMs, Long usageSessionCount, Long avgUsageMs) {
        this.totalUsageMs = totalUsageMs;
        this.usageSessionCount = usageSessionCount;
        this.avgUsageMs = avgUsageMs;
    }
}
