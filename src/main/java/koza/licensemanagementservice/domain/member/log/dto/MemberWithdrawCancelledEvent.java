package koza.licensemanagementservice.domain.member.log.dto;

import lombok.Data;

@Data
public class MemberWithdrawCancelledEvent {
    private final Long memberId;
}
