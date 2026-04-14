package koza.licensemanagementservice.domain.member.log.dto;

import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class MemberStatusChangedEvent {
    private Member target;
    private Member operator;
    private MemberStatus before;
    private MemberStatus after;
    private String reason;
}
