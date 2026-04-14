package koza.licensemanagementservice.domain.member.dto.response;

import koza.licensemanagementservice.domain.member.entity.JoinType;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberGrade;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminMemberSummaryResponse {
    private Long id;
    private String email;
    private String nickname;
    private String profileURL;
    private MemberGrade grade;
    private MemberStatus status;
    private JoinType joinType;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static AdminMemberSummaryResponse from(Member m) {
        return AdminMemberSummaryResponse.builder()
                .id(m.getId())
                .email(m.getEmail())
                .nickname(m.getNickname())
                .profileURL(m.getProfileURL())
                .grade(m.getGrade())
                .status(m.getStatus())
                .joinType(JoinType.from(m.getProvider()))
                .createdAt(m.getCreateAt())
                .lastLoginAt(m.getLastLoginAt())
                .build();
    }
}
