package koza.licensemanagementservice.domain.member.dto.response;

import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberGrade;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
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
    private MemberGrade grade;
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
                .grade(m.getGrade())
                .status(m.getStatus())
                .provider(m.getProvider())
                .createdAt(m.getCreateAt())
                .lastLoginAt(m.getLastLoginAt())
                .build();
    }
}
