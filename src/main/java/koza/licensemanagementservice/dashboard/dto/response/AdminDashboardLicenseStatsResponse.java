package koza.licensemanagementservice.dashboard.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record AdminDashboardLicenseStatsResponse(
        Long totalLicenses, // 총 라이센스 수
        Long allocatedLicenses, // 좌석 점유 라이센스 수
        Long unallocatedLicenses, // 좌석 미점유 라이센스 수
        Long inactiveLicenses, // INACTIVE 상태 라이센스 수
        Long expiredLicenses, // EXPIRED 상태 라이센스 수
        Long activeSessions // 사용중인 라이센스 수(세션 활성화 돼있는)
) {
    @QueryProjection
    public AdminDashboardLicenseStatsResponse {
    }
}
