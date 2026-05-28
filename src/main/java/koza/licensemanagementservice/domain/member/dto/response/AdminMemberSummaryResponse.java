package koza.licensemanagementservice.domain.member.dto.response;

import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import koza.licensemanagementservice.domain.plan.entity.PlanCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminMemberSummaryResponse {
    private Long memberId;
    private String memberEmail;
    private String memberNickname;
    private String profileURL;
    private PlanCode planCode;
    private MemberStatus status;
    private String provider;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static AdminMemberSummaryResponse from(Member m) {
        return AdminMemberSummaryResponse.builder()
                .memberId(m.getId())
                .memberEmail(m.getEmail())
                .memberNickname(m.getNickname())
                .profileURL(m.getProfileURL())
                .planCode(m.getCurrentPlanCode())
                .status(m.getStatus())
                .provider(m.getProvider())
                .createdAt(m.getCreateAt())
                .lastLoginAt(m.getLastLoginAt())
                .build();
    }
}
