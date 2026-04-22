package koza.licensemanagementservice.domain.software.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SoftwareAdminSummaryResponse {
    private Long softwareId;
    private String softwareName;
    private String latestVersion;
    private String ownerEmail;
    private SoftwareStatus status;
    private LocalDateTime createAt;

    @QueryProjection
    public SoftwareAdminSummaryResponse(Long softwareId, String softwareName, String latestVersion, String ownerEmail, SoftwareStatus status, LocalDateTime createAt) {
        this.softwareId = softwareId;
        this.softwareName = softwareName;
        this.latestVersion = latestVersion;
        this.ownerEmail = ownerEmail;
        this.status = status;
        this.createAt = createAt;
    }
}
