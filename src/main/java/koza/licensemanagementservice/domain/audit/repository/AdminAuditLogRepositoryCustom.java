package koza.licensemanagementservice.domain.audit.repository;

import koza.licensemanagementservice.domain.audit.dto.request.AuditSearchCondition;
import koza.licensemanagementservice.domain.audit.dto.response.AuditLogResponse;
import koza.licensemanagementservice.domain.audit.dto.response.RecentAuditResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminAuditLogRepositoryCustom {
    List<RecentAuditResponse> findRecent(int limit);

    Page<AuditLogResponse> search(AuditSearchCondition condition, Pageable pageable);
}
