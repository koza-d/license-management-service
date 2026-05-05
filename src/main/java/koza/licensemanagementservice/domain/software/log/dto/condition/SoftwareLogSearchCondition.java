package koza.licensemanagementservice.domain.software.log.dto.condition;

import koza.licensemanagementservice.domain.software.log.entity.SoftwareLogType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SoftwareLogSearchCondition {
    private final SoftwareLogType logType;
    private final LocalDate from;
    private final LocalDate to;
}
