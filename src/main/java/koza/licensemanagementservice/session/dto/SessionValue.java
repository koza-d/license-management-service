package koza.licensemanagementservice.session.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SessionValue {
    private String sessionId;
    private Long licenseId;
    private String ipAddress;
    private String userAgent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiredAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verifyAt;

    @Setter
    private LocalDateTime latestActiveAt;

}
