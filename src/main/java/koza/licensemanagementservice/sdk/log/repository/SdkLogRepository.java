package koza.licensemanagementservice.sdk.log.repository;

import koza.licensemanagementservice.sdk.log.entity.SdkLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SdkLogRepository extends SdkLogRepositoryCustom, JpaRepository<SdkLog, Long> {

}
