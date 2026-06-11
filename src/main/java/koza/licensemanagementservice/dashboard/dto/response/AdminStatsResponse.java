package koza.licensemanagementservice.dashboard.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public record AdminStatsResponse(
    Long totalMembers,      // 전체 회원 수
    Long totalSoftware,     // 전체 소프트웨어 수
    Long activeSessions,    // 활성 세션 수 (동시접속자 수)
    Long pendingQna         // 답변 대기 중인 문의 수
) {
    @QueryProjection
    public AdminStatsResponse {
    }
}
