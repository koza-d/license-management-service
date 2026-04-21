package koza.licensemanagementservice.stat.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class SoftwareUsageResponse {
    private final String softwareName;
    private final String ownerEmail;
    private final String ownerNickname;
    private final Long sessionCount;
    private final Long usageMinutes;

    @QueryProjection
    public SoftwareUsageResponse(String softwareName, String ownerEmail, String ownerNickname, Long sessionCount, Long usageMinutes) {
        this.softwareName = softwareName;
        this.ownerEmail = ownerEmail;
        this.ownerNickname = ownerNickname;
        this.sessionCount = sessionCount;
        this.usageMinutes = usageMinutes;
    }
}
