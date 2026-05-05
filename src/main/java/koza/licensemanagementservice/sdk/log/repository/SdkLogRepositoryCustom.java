package koza.licensemanagementservice.sdk.log.repository;

import koza.licensemanagementservice.stat.dto.VerificationAttemptTrend;

import java.time.LocalDate;
import java.util.List;

public interface SdkLogRepositoryCustom {
    List<VerificationAttemptTrend> getVerificationMetrics(LocalDate from, LocalDate to);
}
