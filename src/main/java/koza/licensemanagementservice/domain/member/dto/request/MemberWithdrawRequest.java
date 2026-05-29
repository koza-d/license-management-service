package koza.licensemanagementservice.domain.member.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberWithdrawRequest {
    @Size(max = 500, message = "사유는 최대 500자입니다.")
    private final String reason;
}
