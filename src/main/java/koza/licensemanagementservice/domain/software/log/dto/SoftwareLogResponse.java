package koza.licensemanagementservice.domain.software.log.dto;

import com.querydsl.core.annotations.QueryProjection;
import koza.licensemanagementservice.domain.software.log.entity.SoftwareLogType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class SoftwareLogResponse {
    private final SoftwareLogType logType;
    private final String operatorEmail;
    private final String operatorName;
    private final Map<String, Object> data;
    private final LocalDateTime createAt;

    @QueryProjection
    public SoftwareLogResponse(SoftwareLogType logType, String operatorEmail, String operatorName, Map<String, Object> data, LocalDateTime createAt) {
        this.logType = logType;
        this.operatorEmail = operatorEmail;
        this.operatorName = operatorName;
        this.data = data;
        this.createAt = createAt;
    }
}
