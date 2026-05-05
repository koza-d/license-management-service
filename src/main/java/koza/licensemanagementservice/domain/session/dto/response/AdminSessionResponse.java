package koza.licensemanagementservice.domain.session.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminSessionResponse {
    private String sessionId;
    private final Long licenseId;
    private final String licenseKey;
    private final String licenseName;
    private final String memberEmail;
    private final String softwareName;
    private final LocalDateTime latestActiveAt;
    private String ipAddress;

    @QueryProjection
    public AdminSessionResponse(Long licenseId, String licenseKey, String licenseName, String memberEmail, String softwareName, LocalDateTime latestActiveAt) {
        this.licenseId = licenseId;
        this.licenseKey = licenseKey;
        this.licenseName = licenseName;
        this.memberEmail = memberEmail;
        this.softwareName = softwareName;
        this.latestActiveAt = latestActiveAt;
    }
}
