package koza.licensemanagementservice.verification.log.repository;

import koza.licensemanagementservice.verification.log.entity.VerifyLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerifyLogRepository extends VerifyLogRepositoryCustom, JpaRepository<VerifyLog, Long> {

}
