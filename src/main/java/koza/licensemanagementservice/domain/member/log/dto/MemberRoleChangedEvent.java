package koza.licensemanagementservice.domain.member.log.dto;

import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class MemberRoleChangedEvent {
    private Member target;
    private Member operator;
    private MemberRole before;
    private MemberRole after;
    private String reason;
}
