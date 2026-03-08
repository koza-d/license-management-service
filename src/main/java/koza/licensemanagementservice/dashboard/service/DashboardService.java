package koza.licensemanagementservice.dashboard.service;

import koza.licensemanagementservice.dashboard.dto.DashboardStatsResponse;
import koza.licensemanagementservice.license.entity.LicenseStatus;
import koza.licensemanagementservice.license.repository.LicenseRepository;
import koza.licensemanagementservice.member.dto.CustomUser;
import koza.licensemanagementservice.software.repository.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
    private final LicenseRepository licenseRepository;
    private final SoftwareRepository softwareRepository;

    public DashboardStatsResponse getStats(CustomUser user) {
        // 대시보드 주요 지표
        Long memberId = user.getId();
        Long total = licenseRepository.countBySoftware_MemberId(memberId);
        Long active = licenseRepository.countBySoftware_MemberIdAndStatusEquals(memberId, LicenseStatus.ACTIVE);
        Long banned = licenseRepository.countBySoftware_MemberIdAndStatusEquals(memberId, LicenseStatus.BANNED);
        Long expired = licenseRepository.countBySoftware_MemberIdAndStatusAndExpiredAtBefore(memberId, LicenseStatus.ACTIVE,LocalDateTime.now());
        Long activeSessions = licenseRepository.countBySoftware_MemberIdAndHasActiveSessionTrue(memberId);

        return new DashboardStatsResponse(total, active, banned, expired, activeSessions);
    }

}
