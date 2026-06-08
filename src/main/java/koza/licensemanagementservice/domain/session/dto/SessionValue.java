package koza.licensemanagementservice.domain.session.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class SessionValue {
    private String sessionId;
    private Long licenseId;
    private String ipAddress;

    @Builder.Default
    private String userAgent = "Unknown";
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiredAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verifyAt;

    @Setter
    private LocalDateTime latestActiveAt;

    @Builder.Default
    private Map<String, String> changedLocalVariables = new HashMap<>(); // 변경된 로컬 변수만 저장

    @Setter
    private byte[] keyC2S;

    @Setter
    private byte[] keyS2C;
}
