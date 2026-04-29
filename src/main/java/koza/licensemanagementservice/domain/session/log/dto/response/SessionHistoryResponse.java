package koza.licensemanagementservice.domain.session.log.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import koza.licensemanagementservice.domain.session.log.entity.ReleaseType;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class SessionHistoryResponse {
    private final String sessionId;
    private final String ipAddress;
    private final String userAgent;
    private final LocalDateTime verifyAt;
    private final LocalDateTime releaseAt;
    private final ReleaseType releaseType;
    private final String releaseTypeLabel; // "정상적으로 해제" 등
    private final long durationMinutes; // releaseAt - verifyAt

    @QueryProjection
    public SessionHistoryResponse(String sessionId, String ipAddress, String userAgent, LocalDateTime verifyAt, LocalDateTime releaseAt, ReleaseType releaseType) {
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.verifyAt = verifyAt;
        this.releaseAt = releaseAt;
        this.releaseType = releaseType;
        this.releaseTypeLabel = releaseType.getDesc();
        this.durationMinutes = Duration.between(verifyAt, releaseAt).toMinutes();
    }
}
