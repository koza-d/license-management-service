package koza.licensemanagementservice.domain.license.log.dto;

import com.querydsl.core.annotations.QueryProjection;
import koza.licensemanagementservice.domain.license.log.entity.LicenseLogType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class LicenseLogResponse {
    private final LicenseLogType logType;
    private final String operatorEmail;
    private final String operatorName;
    private final Map<String, Object> data;
    private final LocalDateTime createAt;

    @QueryProjection
    public LicenseLogResponse(LicenseLogType logType, String operatorEmail, String operatorName, Map<String, Object> data, LocalDateTime createAt) {
        this.logType = logType;
        this.operatorEmail = operatorEmail;
        this.operatorName = operatorName;
        this.data = data;
        this.createAt = createAt;
    }
}
