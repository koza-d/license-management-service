package koza.licensemanagementservice.domain.member.dto.response;

import koza.licensemanagementservice.domain.member.entity.JoinType;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberGrade;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminMemberDetailResponse {
    private Long id;
    private String email;
    private String nickname;
    private String profileURL;
    private MemberGrade grade;
    private MemberStatus status;
    private JoinType joinType;
    private String providerId;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static AdminMemberDetailResponse from(Member m) {
        return AdminMemberDetailResponse.builder()
                .id(m.getId())
                .email(m.getEmail())
                .nickname(m.getNickname())
                .profileURL(m.getProfileURL())
                .grade(m.getGrade())
                .status(m.getStatus())
                .joinType(JoinType.from(m.getProvider()))
                .providerId(m.getProviderId())
                .roles(m.getRoles())
                .createdAt(m.getCreateAt())
                .lastLoginAt(m.getLastLoginAt())
                .build();
    }
}
