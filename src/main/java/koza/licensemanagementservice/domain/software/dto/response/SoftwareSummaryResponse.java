package koza.licensemanagementservice.domain.software.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SoftwareSummaryResponse {
    private Long id;
    private String name;
    private String version;
    private SoftwareStatus status;
    private LocalDateTime statusUntil;
    private int licenseCount;
    private int activeSessionCount;
    private LocalDateTime createAt;

    @QueryProjection
    public SoftwareSummaryResponse(Long id, String name, String version, SoftwareStatus status, LocalDateTime statusUntil, int licenseCount, int activeSessionCount, LocalDateTime createAt) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.status = status;
        this.statusUntil = statusUntil;
        this.licenseCount = licenseCount;
        this.activeSessionCount = activeSessionCount;
        this.createAt = createAt;
    }
}
