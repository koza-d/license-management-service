package koza.licensemanagementservice.domain.session.dto.response;

import koza.licensemanagementservice.domain.session.dto.SessionValue;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SessionAdminDetailResponse {
    private String sessionId;
    private String memberEmail;
    private String softwareName;
    private String licenseKey;
    private String licenseName;
    private LocalDateTime startedAt;
    private String ipAddress;
    private String userAgent;

    public static SessionAdminDetailResponse of(SessionValue session,
                                                String memberEmail,
                                                String softwareName,
                                                String licenseKey,
                                                String licenseName) {
        return SessionAdminDetailResponse.builder()
                .sessionId(session.getSessionId())
                .memberEmail(memberEmail)
                .softwareName(softwareName)
                .licenseKey(licenseKey)
                .licenseName(licenseName)
                .startedAt(session.getVerifyAt())
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .build();
    }
}
