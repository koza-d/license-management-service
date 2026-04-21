package koza.licensemanagementservice.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberWithdrawRequest {
    private final String reason;
}
