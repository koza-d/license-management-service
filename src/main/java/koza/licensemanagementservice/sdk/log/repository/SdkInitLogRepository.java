package koza.licensemanagementservice.sdk.log.repository;

import koza.licensemanagementservice.sdk.log.entity.SdkInitLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SdkInitLogRepository extends JpaRepository<SdkInitLog, Long> {
}
