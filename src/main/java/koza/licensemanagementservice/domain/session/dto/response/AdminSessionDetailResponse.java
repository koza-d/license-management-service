package koza.licensemanagementservice.domain.session.dto.response;

import koza.licensemanagementservice.domain.session.dto.SessionValue;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminSessionDetailResponse {
    private String sessionId;
    private String memberEmail;
    private String softwareName;
    private String licenseKey;
    private String licenseName;
    private LocalDateTime startedAt;
    private String ipAddress;
    private String userAgent;

    public static AdminSessionDetailResponse of(SessionValue session,
                                                String memberEmail,
                                                String softwareName,
                                                String licenseKey,
                                                String licenseName) {
        return AdminSessionDetailResponse.builder()
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
