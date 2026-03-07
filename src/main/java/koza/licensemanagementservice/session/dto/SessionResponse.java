package koza.licensemanagementservice.session.dto;

import koza.licensemanagementservice.verification.status.SessionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SessionResponse {
    private String sessionId;
    private String licenseKey;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime verifyAt;
    private LocalDateTime expireAt;
    private LocalDateTime latestActiveAt;
    private SessionStatus status;
}
