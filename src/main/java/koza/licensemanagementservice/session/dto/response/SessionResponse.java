package koza.licensemanagementservice.session.dto.response;

import koza.licensemanagementservice.session.dto.SessionValue;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SessionResponse {
    private String sessionId;
    private String licenseKey;
    private String licenseName;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime verifyAt;
    private LocalDateTime expireAt;
    private LocalDateTime latestActiveAt;

    public static SessionResponse of(SessionValue session, String sessionId, String licenseKey, String licenseName) {
        return SessionResponse.builder()
                .sessionId(sessionId)
                .licenseKey(licenseKey)
                .licenseName(licenseName)
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .verifyAt(session.getVerifyAt())
                .expireAt(session.getExpiredAt())
                .latestActiveAt(session.getLatestActiveAt())
                .build();
    }
}
