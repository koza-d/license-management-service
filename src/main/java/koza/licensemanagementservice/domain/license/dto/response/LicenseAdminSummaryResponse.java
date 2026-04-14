package koza.licensemanagementservice.domain.license.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
public class LicenseAdminSummaryResponse {
    private Long licenseId;
    private String softwareOwnerEmail;
    private String softwareName;
    private String licenseName;
    private String licenseKey;
    private LocalDateTime createAt;
    private LocalDateTime expiredAt;
    private boolean hasActiveSession;
    private LocalDateTime latestActiveAt;
    private String status;

    @QueryProjection
    public LicenseAdminSummaryResponse(Long licenseId, String softwareOwnerEmail, String softwareName, String licenseName, String licenseKey, LocalDateTime createAt, LocalDateTime expiredAt, boolean hasActiveSession, LocalDateTime latestActiveAt, String status) {
        this.licenseId = licenseId;
        this.softwareOwnerEmail = softwareOwnerEmail;
        this.softwareName = softwareName;
        this.licenseName = licenseName;
        this.licenseKey = licenseKey;
        this.createAt = createAt;
        this.expiredAt = expiredAt;
        this.hasActiveSession = hasActiveSession;
        this.latestActiveAt = latestActiveAt;
        this.status = status;
    }
}
