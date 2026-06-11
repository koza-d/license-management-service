package koza.licensemanagementservice.domain.license.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record LicenseStatsResponse(
    Long total, // 총 라이센스 수
    Long expire, // 만료 라이센스 수
    Long inactive, // INACTIVE 라이센스 수
    Long active, // 상태 'ACTIVE' 라이센스 수
    Long banned, // 상태 'BANNED' 라이센스 수
    Long activeSessions // 사용중인 라이센스 수(세션 유효)
) {
    @QueryProjection
    public LicenseStatsResponse {
    }
}
