package koza.licensemanagementservice.domain.software.log.repository;

import koza.licensemanagementservice.domain.software.log.dto.SoftwareLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SoftwareLogRepositoryCustom {
    Page<SoftwareLogResponse> findBySoftwareId(Long softwareId, SoftwareLogSearchCondition condition, Pageable pageable);
}
