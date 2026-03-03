package koza.licensemanagementservice.verification.repository;

import koza.licensemanagementservice.verification.entity.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {

}
