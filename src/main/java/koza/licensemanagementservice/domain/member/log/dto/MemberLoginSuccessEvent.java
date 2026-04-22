package koza.licensemanagementservice.domain.member.log.dto;

import koza.licensemanagementservice.domain.member.entity.JoinType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class MemberLoginSuccessEvent {
    private Long memberId;
    private JoinType joinType;
    private String ipAddress;
    private String userAgent;
}
