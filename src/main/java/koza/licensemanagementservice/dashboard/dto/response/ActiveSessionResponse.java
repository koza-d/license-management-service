package koza.licensemanagementservice.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ActiveSessionResponse {
    private String sessionId;
    private Long licenseId;
    private String licenseName;
    private String licenseKey;
    private String softwareName;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime verifyAt;
    private LocalDateTime expireAt;
    private LocalDateTime latestActiveAt;
}
