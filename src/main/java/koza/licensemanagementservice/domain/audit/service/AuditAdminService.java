package koza.licensemanagementservice.domain.audit.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.audit.dto.request.AuditSearchCondition;
import koza.licensemanagementservice.domain.audit.dto.response.AuditLogResponse;
import koza.licensemanagementservice.domain.audit.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static koza.licensemanagementservice.global.validation.ValidUserAuthorized.validAdminAuthorized;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditAdminService {
    private final AdminAuditLogRepository auditLogRepository;

    public Page<AuditLogResponse> search(CustomUser admin, AuditSearchCondition condition, Pageable pageable) {
        validAdminAuthorized(admin);
        return auditLogRepository.search(condition, pageable);
    }
}
