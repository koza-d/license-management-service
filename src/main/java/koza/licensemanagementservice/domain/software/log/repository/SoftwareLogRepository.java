package koza.licensemanagementservice.domain.software.log.repository;

import koza.licensemanagementservice.domain.software.log.entity.SoftwareLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoftwareLogRepository extends JpaRepository<SoftwareLog, Long> {
}
