package koza.licensemanagementservice.dashboard.dto;

import com.querydsl.core.annotations.QueryProjection;
import koza.licensemanagementservice.session.dto.DailyUsageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SoftwareStatsResponse {
    private Long softwareId;
    private String softwareName;
    private Long licenseCount;
    private Long activeSessionCount;
    private Long totalUsageMinutes;
    private List<DailyUsageResponse> dailyStats = new ArrayList<>();

    @QueryProjection
    public SoftwareStatsResponse(Long softwareId, String softwareName, Long licenseCount, Long activeSessionCount, Long totalUsageMinutes) {
        this.softwareId = softwareId;
        this.softwareName = softwareName;
        this.licenseCount = licenseCount;
        this.activeSessionCount = activeSessionCount;
        this.totalUsageMinutes = totalUsageMinutes;
    }
}
