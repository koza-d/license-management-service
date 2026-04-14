package koza.licensemanagementservice.domain.license.log.repository;

import koza.licensemanagementservice.domain.license.log.entity.LicenseExtendLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LicenseExtendLogRepository extends LicenseExtendLogRepositoryCustom, JpaRepository<LicenseExtendLog, Long> {
    Page<LicenseExtendLog> findByLicenseIdAndCreateAtBetween(Long licenseId, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
