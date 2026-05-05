package koza.licensemanagementservice.domain.audit.repository;

import koza.licensemanagementservice.domain.audit.dto.condition.AuditSearchCondition;
import koza.licensemanagementservice.domain.audit.dto.response.AdminAuditLogResponse;
import koza.licensemanagementservice.domain.audit.dto.response.AdminRecentAuditResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminAuditLogRepositoryCustom {
    List<AdminRecentAuditResponse> findRecent(int limit);

    Page<AdminAuditLogResponse> search(AuditSearchCondition condition, Pageable pageable);
}
