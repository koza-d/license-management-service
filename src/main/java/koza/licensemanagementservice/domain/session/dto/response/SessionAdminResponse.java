package koza.licensemanagementservice.domain.session.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import koza.licensemanagementservice.domain.session.dto.SessionValue;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SessionAdminListResponse {
    private String sessionId;
    private final String memberEmail;
    private final String softwareName;
    private final String licenseKey;
    private final String licenseName;
    private final LocalDateTime startedAt;
    private String ipAddress;

    @QueryProjection
    public SessionAdminListResponse(String memberEmail, String softwareName, String licenseKey, String licenseName, LocalDateTime startedAt) {
        this.memberEmail = memberEmail;
        this.softwareName = softwareName;
        this.licenseKey = licenseKey;
        this.licenseName = licenseName;
        this.startedAt = startedAt;
    }
}
