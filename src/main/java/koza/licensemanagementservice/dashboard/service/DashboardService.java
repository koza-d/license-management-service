package koza.licensemanagementservice.dashboard.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.dashboard.dto.response.ActiveSessionResponse;
import koza.licensemanagementservice.dashboard.dto.response.ExpiringLicenseResponse;
import koza.licensemanagementservice.dashboard.dto.response.LicenseStatsResponse;
import koza.licensemanagementservice.dashboard.dto.response.SoftwareUsageResponse;
import koza.licensemanagementservice.dashboard.dto.response.VendorStatsResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseStatusCount;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.session.dto.SessionValue;
import koza.licensemanagementservice.domain.session.service.SessionManager;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
    private static final int EXPIRING_SOON_DAYS = 7;
    private static final int EXPIRING_LIMIT_MAX = 30;
    private static final int ACTIVE_SESSION_LIMIT_MAX = 30;
    private static final int SOFTWARE_USAGE_LIMIT_MAX = 10;
    private static final Set<Integer> ALLOWED_USAGE_DAYS = Set.of(7, 30);

    private final LicenseRepository licenseRepository;
    private final SoftwareRepository softwareRepository;
    private final SessionManager sessionManager;

    public VendorStatsResponse getStats(CustomUser user) {
        Long memberId = user.getId();
        long totalSoftware = softwareRepository.countByMemberId(memberId);
        long totalLicenses = licenseRepository.countBySoftware_MemberId(memberId);
        long activeSessions = licenseRepository.countActiveSessionLicensesByMember(memberId);

        return new VendorStatsResponse(totalSoftware, totalLicenses, activeSessions);
    }

    public LicenseStatsResponse getLicenseStats(CustomUser user) {
        Long memberId = user.getId();
        LocalDateTime now = LocalDateTime.now();

        long active = 0L, expired = 0L, banned = 0L;
        for (LicenseStatusCount row : licenseRepository.countLicensesByStatusForMember(memberId)) {
            switch (row.getStatus()) {
                case ACTIVE -> active = row.getCount();
                case EXPIRED -> expired = row.getCount();
                case BANNED -> banned = row.getCount();
            }
        }

        long inUse = licenseRepository.countActiveSessionLicensesByMember(memberId);
        long expiringWithin7d = licenseRepository.countExpiringSoonLicensesByMember(
                memberId, now, now.plusDays(EXPIRING_SOON_DAYS));

        return LicenseStatsResponse.of(active, expired, banned, inUse, expiringWithin7d);
    }

    public List<ExpiringLicenseResponse> getExpiringSoonLicenses(CustomUser user, int limit) {
        int safeLimit = clamp(limit, 1, EXPIRING_LIMIT_MAX);
        return licenseRepository.findExpiringSoonLicensesByMember(
                user.getId(), LocalDateTime.now(), safeLimit);
    }

    public List<ActiveSessionResponse> getActiveSessions(CustomUser user, int limit) {
        int safeLimit = clamp(limit, 1, ACTIVE_SESSION_LIMIT_MAX);
        List<License> licenses = licenseRepository.findActiveSessionLicensesByMember(user.getId(), safeLimit);

        List<ActiveSessionResponse> result = new ArrayList<>(licenses.size());
        for (License license : licenses) {
            Optional<SessionValue> session = sessionManager.getSessionByLicenseId(license.getId());
            if (session.isEmpty()) continue; // DB 플래그와 Redis 불일치 — 스킵

            SessionValue sv = session.get();
            result.add(new ActiveSessionResponse(
                    sv.getSessionId(),
                    license.getSoftware().getMember().getNickname(),
                    license.getId(),
                    license.getName(),
                    license.getLicenseKey(),
                    license.getSoftware().getName(),
                    sv.getIpAddress(),
                    sv.getUserAgent(),
                    sv.getVerifyAt(),
                    sv.getExpiredAt(),
                    sv.getLatestActiveAt()
            ));
        }
        return result;
    }

    public List<SoftwareUsageResponse> getSoftwareUsage(CustomUser user, int days, int limit) {
        if (!ALLOWED_USAGE_DAYS.contains(days))
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);

        int safeLimit = clamp(limit, 1, SOFTWARE_USAGE_LIMIT_MAX);
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return softwareRepository.findSoftwareUsageByMember(user.getId(), since, safeLimit);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
