package koza.licensemanagementservice.domain.audit.repository;

import koza.licensemanagementservice.domain.audit.entity.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long>, AdminAuditLogRepositoryCustom {
}
