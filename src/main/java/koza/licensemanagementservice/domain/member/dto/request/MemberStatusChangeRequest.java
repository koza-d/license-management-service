package koza.licensemanagementservice.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberStatusChangeRequest {
    @NotNull(message = "변경할 상태는 필수입니다.")
    private MemberStatus status;

    private String reason;
}
