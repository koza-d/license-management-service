package koza.licensemanagementservice.domain.member.log.dto;

import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class MemberGradeChangedEvent {
    private Member target;
    private Member operator;
    private MemberGrade before;
    private MemberGrade after;
    private String reason;
}
