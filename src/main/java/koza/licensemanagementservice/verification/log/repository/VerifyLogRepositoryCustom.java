package koza.licensemanagementservice.verification.log.repository;

import koza.licensemanagementservice.stat.dto.VerificationAttemptTrend;

import java.time.LocalDate;
import java.util.List;

public interface VerifyLogRepositoryCustom {
    List<VerificationAttemptTrend> getVerificationMetrics(LocalDate from, LocalDate to);
}
