package koza.licensemanagementservice.license.repository;


import koza.licensemanagementservice.license.entity.License;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LicenseRepositoryCustom {
    Optional<License> findByIdWithSoftwareAndMember(Long licenseId);
    Optional<License> findByLicenseKeyWithSoftware(String licenseKey);
    List<License> findByIdInWithSoftwareWithMember(List<Long> ids);
    List<License> findByMemberId(Long memberId);
    Page<License> findByMemberId(Long memberId, String search, Boolean hasActiveSession, Integer expireWithin, Pageable pageable);
    Page<License> findBySoftwareId(Long softwareId, String search, Boolean hasActiveSession, Pageable pageable);
}
