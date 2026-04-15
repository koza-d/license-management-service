package koza.licensemanagementservice.domain.software.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class SoftwareAdminStatsResponse {
    private final Long totalUsageSeconds;
    private final Long usageSessionCount;
    private final Long avgUsageSeconds;

    @QueryProjection
    public SoftwareAdminStatsResponse(Long totalUsageSeconds, Long usageSessionCount, Long avgUsageSeconds) {
        this.totalUsageSeconds = totalUsageSeconds;
        this.usageSessionCount = usageSessionCount;
        this.avgUsageSeconds = avgUsageSeconds;
    }
}
