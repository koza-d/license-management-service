package koza.licensemanagementservice.domain.license.repository;

import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long>, LicenseRepositoryCustom {
    Page<License> findBySoftwareId(Long softwareId, Pageable pageable);
    Page<License> findBySoftwareIdAndHasActiveSessionIsTrue(Long softwareId, Pageable pageable);
    Page<License> findBySoftware_MemberIdAndHasActiveSessionIsTrue(Long memberId, Pageable pageable);
    List<License> findBySoftwareIdAndHasActiveSessionIsTrue(Long softwareId);

    @Query("SELECT l.id FROM License l WHERE l.hasActiveSession = true")
    List<Long> findIdsByHasActiveSessionTrue();

    int countBySoftwareId(Long softwareId);

    /**
     * 최종 사용자에게 할당된(좌석을 차지하는) 라이센스 수 카운팅
     * @return
     */


    @Query("SELECT COUNT(l) FROM License l " +
            "WHERE l.software.member.id = :memberId " +
            "AND (l.status = koza.licensemanagementservice.domain.license.entity.LicenseStatus.ACTIVE " +
            "OR (l.status = 'BANNED' AND l.statusUntil IS NOT NULL))")
    long countAllocatedLicenses(@Param("memberId") Long memberId);

    Long countBySoftwareIdAndStatusEquals(Long softwareId, LicenseStatus status);
    Long countBySoftwareIdAndExpiredAtBefore(Long softwareId, LocalDateTime at);
    Long countBySoftwareIdAndHasActiveSessionTrue(Long softwareId);

    @Query("SELECT COUNT(l) FROM License l " +
            "WHERE l.software.member.id = :memberId ")
    Long countByMemberId(@Param("memberId") Long memberId);
    boolean existsByLicenseKey(String licenseKey);
}
