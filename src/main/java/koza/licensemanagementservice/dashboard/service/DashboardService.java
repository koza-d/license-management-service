package koza.licensemanagementservice.dashboard.service;

import koza.licensemanagementservice.dashboard.dto.response.DashboardStatsResponse;
import koza.licensemanagementservice.dashboard.dto.response.SoftwareStatsResponse;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.session.log.dto.response.DailyUsageResponse;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
    private final LicenseRepository licenseRepository;
    private final SoftwareRepository softwareRepository;

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public List<SoftwareStatsResponse> getSoftwareStats(CustomUser user) {
        Long memberId = user.getId();
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        Map<Long, List<DailyUsageResponse>> dailyUsageMap = new HashMap<>();

        softwareRepository.findDailyUsageByMemberId(memberId, startDate) // 소프트웨어별 일일 사용량
                .forEach(softwareDailyUsage -> {
                    Long softwareId = softwareDailyUsage.getSoftwareId();
                    DailyUsageResponse dailyUsageResponse = new DailyUsageResponse(softwareDailyUsage.getDate(), softwareDailyUsage.getMinutes());
                    dailyUsageMap.computeIfAbsent(softwareId, k -> new ArrayList<>())
                            .add(dailyUsageResponse);
                });

        return softwareRepository.findSoftwareStatsByMemberId(memberId).stream() // 소프트웨어별 주요 지표 + 소프트웨어별 일일 사용량 merge
                .map(stat -> {
                    stat.getDailyStats().addAll(
                            dailyUsageMap.getOrDefault(stat.getSoftwareId(), List.of())
                    );
                    return stat;
                }).toList();
    }

}
