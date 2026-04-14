package koza.licensemanagementservice.domain.license.log.repository;

import koza.licensemanagementservice.domain.license.log.entity.LicenseLogType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LicenseLogSearchCondition {
    private LicenseLogType logType;
    private LocalDate from;
    private LocalDate to;
}
