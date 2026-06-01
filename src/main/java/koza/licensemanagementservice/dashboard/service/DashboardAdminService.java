package koza.licensemanagementservice.dashboard.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.dashboard.dto.response.AdminDashboardLicenseStatsResponse;
import koza.licensemanagementservice.dashboard.repository.DashboardRepository;
import koza.licensemanagementservice.domain.audit.dto.response.AdminRecentAuditResponse;
import koza.licensemanagementservice.domain.audit.repository.AdminAuditLogRepository;
import koza.licensemanagementservice.dashboard.dto.response.AdminStatsResponse;
import koza.licensemanagementservice.dashboard.dto.response.PendingQnaResponse;
import koza.licensemanagementservice.domain.qna.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static koza.licensemanagementservice.global.validation.ValidUserAuthorized.validAdminAuthorized;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardAdminService {
    private static final int PENDING_QNA_MIN_LIMIT = 1;
    private static final int PENDING_QNA_MAX_LIMIT = 50;
    private static final int RECENT_AUDIT_MIN_LIMIT = 1;
    private static final int RECENT_AUDIT_MAX_LIMIT = 50;

    private final QnaRepository qnaRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final DashboardRepository dashboardRepository;

    public List<PendingQnaResponse> getPendingQna(CustomUser admin, int limit) {
        validAdminAuthorized(admin);
        int safeLimit = Math.min(Math.max(limit, PENDING_QNA_MIN_LIMIT), PENDING_QNA_MAX_LIMIT);
        return qnaRepository.findPendingForDashboard(safeLimit);
    }

    public List<AdminRecentAuditResponse> getRecentAudit(CustomUser admin, int limit) {
        validAdminAuthorized(admin);
        int safeLimit = Math.min(Math.max(limit, RECENT_AUDIT_MIN_LIMIT), RECENT_AUDIT_MAX_LIMIT);
        return auditLogRepository.findRecent(safeLimit);
    }

    public AdminStatsResponse getStats(CustomUser admin) {
        validAdminAuthorized(admin);
        return dashboardRepository.getAdminStats();
    }

    public AdminDashboardLicenseStatsResponse getLicenseStats(CustomUser admin) {
        validAdminAuthorized(admin);
        return dashboardRepository.getAdminLicenseStats();
    }
}
