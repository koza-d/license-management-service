package koza.licensemanagementservice.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class AdminStatsResponse {
    private Long totalLicenses;        // 전체 라이센스 수
    private Long activeLicenses;       // ACTIVE 라이센스 수
    private Long bannedLicenses;       // BANNED 라이센스 수
    private Long expiredLicenses;      // 기간 만료된 라이센스 수
    private Long activeSessions;       // 활성 세션 수
    private Long totalMembers;         // 전체 회원 수
    private Long pendingQna;           // 답변 대기 중인 문의 수
    private Long urgentPendingQna;     // 긴급 + 답변 대기 중인 문의 수
}
