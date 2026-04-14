package koza.licensemanagementservice.domain.license.log.repository;

import koza.licensemanagementservice.domain.license.log.entity.LicenseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenseLogRepository extends LicenseLogRepositoryCustom, JpaRepository<LicenseLog, Long> {
}
