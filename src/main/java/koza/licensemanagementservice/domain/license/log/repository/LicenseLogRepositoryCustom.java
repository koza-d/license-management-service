package koza.licensemanagementservice.domain.license.log.repository;

import koza.licensemanagementservice.domain.license.log.dto.LicenseLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LicenseLogRepositoryCustom {
    Page<LicenseLogResponse> findByLicenseId(Long licenseId, LicenseLogSearchCondition condition, Pageable pageable);
}
