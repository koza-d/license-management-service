package koza.licensemanagementservice.domain.license.repository;


import koza.licensemanagementservice.dashboard.dto.response.ExpiringLicenseResponse;
import koza.licensemanagementservice.domain.license.dto.response.AdminLicenseSummaryResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseStatusCount;
import koza.licensemanagementservice.domain.license.dto.response.LicenseStatsResponse;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.dto.condition.LicenseSearchCondition;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.domain.session.dto.response.AdminSessionResponse;
import koza.licensemanagementservice.domain.session.dto.condition.SessionSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LicenseRepositoryCustom {
    Optional<License> findByIdWithSoftwareAndMember(Long licenseId);
    Optional<License> findByLicenseKeyWithSoftware(String licenseKey);
    List<License> findByIdInWithSoftwareWithMember(List<Long> ids);
    Page<License> findByMemberId(Long memberId, String search, Boolean hasActiveSession, Integer expireWithin, Pageable pageable);
    Page<License> findBySoftwareId(Long softwareId, String search, Boolean hasActiveSession, Pageable pageable);
    Page<AdminLicenseSummaryResponse> findByAllCondition(LicenseSearchCondition condition, Pageable pageable);

    Page<AdminSessionResponse> findActiveSessionLicensesByCondition(SessionSearchCondition condition, Pageable pageable);
    List<License> bulkUpdateExpiredStatus(LocalDateTime now);
    List<License> bulkTransitionStatus(LicenseStatus from, LicenseStatus to, LocalDateTime now);

    List<LicenseStatusCount> countLicensesByStatusForMember(Long memberId);
    long countActiveSessionLicensesByMember(Long memberId);
    long countExpiringSoonLicensesByMember(Long memberId, LocalDateTime now, LocalDateTime threshold);
    List<ExpiringLicenseResponse> findExpiringSoonLicensesByMember(Long memberId, LocalDateTime now, int limit);
    List<License> findActiveSessionLicensesByMember(Long memberId, int limit);

    LicenseStatsResponse getLicenseStatsBySoftwareId(Long softwareId);
}
