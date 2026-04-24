package koza.licensemanagementservice.dashboard.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.audit.dto.response.RecentAuditResponse;
import koza.licensemanagementservice.domain.audit.repository.AdminAuditLogRepository;
import koza.licensemanagementservice.dashboard.dto.AdminStatsResponse;
import koza.licensemanagementservice.dashboard.dto.PendingQnaResponse;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.qna.entity.QnaPriority;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import koza.licensemanagementservice.domain.qna.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    private final LicenseRepository licenseRepository;
    private final MemberRepository memberRepository;
    private final QnaRepository qnaRepository;
    private final AdminAuditLogRepository auditLogRepository;

    public List<PendingQnaResponse> getPendingQna(CustomUser admin, int limit) {
        validAdminAuthorized(admin);
        int safeLimit = Math.min(Math.max(limit, PENDING_QNA_MIN_LIMIT), PENDING_QNA_MAX_LIMIT);
        return qnaRepository.findPendingForDashboard(safeLimit);
    }

    public List<RecentAuditResponse> getRecentAudit(CustomUser admin, int limit) {
        validAdminAuthorized(admin);
        int safeLimit = Math.min(Math.max(limit, RECENT_AUDIT_MIN_LIMIT), RECENT_AUDIT_MAX_LIMIT);
        return auditLogRepository.findRecent(safeLimit);
    }

    public AdminStatsResponse getStats(CustomUser admin) {
        validAdminAuthorized(admin);
        Long totalLicenses = licenseRepository.count();
        Long activeLicenses = licenseRepository.countByStatusEquals(LicenseStatus.ACTIVE);
        Long bannedLicenses = licenseRepository.countByStatusEquals(LicenseStatus.BANNED);
        Long expiredLicenses = licenseRepository.countByStatusAndExpiredAtBefore(
                LicenseStatus.ACTIVE, LocalDateTime.now());
        Long activeSessions = licenseRepository.countByHasActiveSessionTrue();
        Long totalMembers = memberRepository.count();
        Long pendingQna = qnaRepository.countByStatus(QnaStatus.PENDING);
        Long urgentPendingQna = qnaRepository.countByStatusAndPriority(
                QnaStatus.PENDING, QnaPriority.URGENT);

        return AdminStatsResponse.builder()
                .totalLicenses(totalLicenses)
                .activeLicenses(activeLicenses)
                .bannedLicenses(bannedLicenses)
                .expiredLicenses(expiredLicenses)
                .activeSessions(activeSessions)
                .totalMembers(totalMembers)
                .pendingQna(pendingQna)
                .urgentPendingQna(urgentPendingQna)
                .build();
    }
}
