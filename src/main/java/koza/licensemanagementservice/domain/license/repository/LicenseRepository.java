package koza.licensemanagementservice.domain.license.repository;

import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long>, LicenseRepositoryCustom {
    Optional<License> findByLicenseKey(String licenseKey);
    Page<License> findBySoftwareId(Long softwareId, Pageable pageable);
    Page<License> findBySoftwareIdAndHasActiveSessionIsTrue(Long softwareId, Pageable pageable);
    Page<License> findBySoftware_MemberIdAndHasActiveSessionIsTrue(Long memberId, Pageable pageable);
    List<License> findByIdIn(List<Long> id);
    int countBySoftwareId(Long softwareId);

    Long countBySoftwareIdAndStatusEquals(Long softwareId, LicenseStatus status);
    Long countBySoftwareIdAndExpiredAtBefore(Long softwareId, LocalDateTime at);
    Long countBySoftwareIdAndHasActiveSessionTrue(Long softwareId);
    Long countBySoftware_MemberId(Long memberId);
    Long countBySoftware_MemberIdAndStatusEquals(Long memberId, LicenseStatus status);
    Long countBySoftware_MemberIdAndStatusAndExpiredAtBefore(Long memberId, LicenseStatus status, LocalDateTime at);
    Long countBySoftware_MemberIdAndHasActiveSessionTrue(Long memberId);

    boolean existsByLicenseKey(String licenseKey);
}
