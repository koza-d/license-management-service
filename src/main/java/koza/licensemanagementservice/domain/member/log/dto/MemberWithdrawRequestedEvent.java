package koza.licensemanagementservice.domain.member.log.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberWithdrawRequestedEvent {
    private final Long memberId;
    private final String reason;
    private final LocalDateTime scheduledAt;
}
