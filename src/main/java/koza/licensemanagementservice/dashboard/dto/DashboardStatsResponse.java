package koza.licensemanagementservice.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class DashboardStatsResponse {
    private Long totalLicenses; // 전체 라이센스 수
    private Long activeLicenses; // ACTIVE 라이센스 수
    private Long bannedLicenses; // BANNED 라이센스 수
    private Long expiredLicenses; // 만료된 라이센스 수
    private Long activeSessionCount; // 활성화된 세션 수
}
