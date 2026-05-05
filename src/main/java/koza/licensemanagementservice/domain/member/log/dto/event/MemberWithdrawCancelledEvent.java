package koza.licensemanagementservice.domain.member.log.dto.event;

import lombok.Data;

@Data
public class MemberWithdrawCancelledEvent {
    private final Long memberId;
}
