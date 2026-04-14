package koza.licensemanagementservice.domain.license.repository;


import koza.licensemanagementservice.domain.license.dto.response.LicenseAdminSummaryResponse;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.repository.condition.LicenseSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LicenseRepositoryCustom {
    Optional<License> findByIdWithSoftwareAndMember(Long licenseId);
    Optional<License> findByLicenseKeyWithSoftware(String licenseKey);
    List<License> findByIdInWithSoftwareWithMember(List<Long> ids);
    List<License> findByMemberId(Long memberId);
    Page<License> findByMemberId(Long memberId, String search, Boolean hasActiveSession, Integer expireWithin, Pageable pageable);
    Page<License> findBySoftwareId(Long softwareId, String search, Boolean hasActiveSession, Pageable pageable);
    Page<LicenseAdminSummaryResponse> findByAllCondition(LicenseSearchCondition condition, Pageable pageable);
}
