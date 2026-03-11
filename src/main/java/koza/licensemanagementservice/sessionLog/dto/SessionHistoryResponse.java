package koza.licensemanagementservice.sessionLog.dto;

import koza.licensemanagementservice.sessionLog.entity.ReleaseType;
import koza.licensemanagementservice.sessionLog.entity.SessionLog;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Builder
public class SessionHistoryResponse {
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime verifyAt;
    private LocalDateTime releaseAt;
    private ReleaseType releaseType;
    private String releaseTypeLabel; // "정상적으로 해제" 등
    private long durationMinutes; // releaseAt - verifyAt

    public static SessionHistoryResponse from(SessionLog log) {
        return SessionHistoryResponse.builder()
                .sessionId(log.getSessionId())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .verifyAt(log.getVerifyAt())
                .releaseAt(log.getReleaseAt())
                .releaseType(log.getReleaseType())
                .releaseTypeLabel(log.getReleaseType().getDesc())
                .durationMinutes(Duration.between(log.getVerifyAt(), log.getReleaseAt()).toMinutes())
                .build();
    }

}
