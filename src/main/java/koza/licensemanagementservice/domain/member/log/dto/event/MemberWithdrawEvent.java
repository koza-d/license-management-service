package koza.licensemanagementservice.domain.member.log.dto.event;

import koza.licensemanagementservice.domain.member.entity.MemberGrade;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberWithdrawEvent {
    private final Long memberId;
    private final String provider;
    private final MemberGrade grade;
    private final String reason;
    private final LocalDateTime registerAt;
}
