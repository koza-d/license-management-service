package koza.licensemanagementservice.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import koza.licensemanagementservice.domain.member.entity.MemberRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberRoleChangeRequest {
    @NotNull(message = "변경할 역할은 필수입니다.")
    private MemberRole role;

    @NotBlank(message = "사유는 필수입니다.")
    @Size(max = 500, message = "사유는 최대 500자입니다.")
    private String reason;
}
