package koza.licensemanagementservice.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;
import koza.licensemanagementservice.domain.member.entity.MemberRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberRoleChangeRequest {
    @NotNull(message = "변경할 역할은 필수입니다.")
    private MemberRole role;

    private String reason;
}
