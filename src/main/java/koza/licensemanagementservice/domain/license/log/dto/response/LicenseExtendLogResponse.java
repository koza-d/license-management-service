package koza.licensemanagementservice.domain.license.log.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LicenseExtendLogResponse {
    private String operatorEmail;
    private String operatorName;
    private LocalDateTime beforeExpiredAt;
    private LocalDateTime afterExpiredAt;
    private Long periodMs;
    private LocalDateTime createAt;

    @QueryProjection
    public LicenseExtendLogResponse(String operatorEmail, String operatorName, LocalDateTime beforeExpiredAt, LocalDateTime afterExpiredAt, Long periodMs, LocalDateTime createAt) {
        this.operatorEmail = operatorEmail;
        this.operatorName = operatorName;
        this.beforeExpiredAt = beforeExpiredAt;
        this.afterExpiredAt = afterExpiredAt;
        this.periodMs = periodMs;
        this.createAt = createAt;
    }
}
