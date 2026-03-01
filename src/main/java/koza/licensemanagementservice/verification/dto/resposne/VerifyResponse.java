package koza.licensemanagementservice.verification.dto.resposne;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class VerifyResponse {
    private String sessionId;
    private LocalDateTime exp;
    private LocalDateTime serverTime;
    private Long remainMs;
    private Map<String, Object> metadata;
}
