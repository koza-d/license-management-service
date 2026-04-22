package koza.licensemanagementservice.domain.license.log.repository;

import koza.licensemanagementservice.domain.license.log.dto.LicenseLogResponse;
import koza.licensemanagementservice.stat.dto.LicenseStatusTrendResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface LicenseLogRepositoryCustom {
    Page<LicenseLogResponse> findByLicenseId(Long licenseId, LicenseLogSearchCondition condition, Pageable pageable);
    List<LicenseStatusTrendResponse> getLicenseStatusTrendsByDate(LocalDate from, LocalDate to);
}
