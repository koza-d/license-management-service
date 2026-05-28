package koza.licensemanagementservice.domain.member.log.dto.event;

import koza.licensemanagementservice.domain.plan.entity.PlanCode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberWithdrawEvent {
    private final Long memberId;
    private final String provider;
    private final PlanCode planCode;
    private final String reason;
    private final LocalDateTime registerAt;
}
