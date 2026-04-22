package koza.licensemanagementservice.domain.audit.service;

import koza.licensemanagementservice.domain.audit.dto.request.AuditSearchCondition;
import koza.licensemanagementservice.domain.audit.dto.response.AuditLogResponse;
import koza.licensemanagementservice.domain.audit.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditAdminService {
    private final AdminAuditLogRepository auditLogRepository;

    public Page<AuditLogResponse> search(AuditSearchCondition condition, Pageable pageable) {
        return auditLogRepository.search(condition, pageable);
    }
}
