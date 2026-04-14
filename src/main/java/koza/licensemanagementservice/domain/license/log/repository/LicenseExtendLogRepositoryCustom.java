package koza.licensemanagementservice.domain.license.log.repository;

import koza.licensemanagementservice.domain.license.log.dto.LicenseExtendLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface LicenseExtendLogRepositoryCustom {
    Page<LicenseExtendLogResponse> findByLicenseId(Long licenseId, LocalDate from, LocalDate to, Pageable pageable);
}
