package koza.licensemanagementservice.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;
import koza.licensemanagementservice.domain.member.entity.MemberGrade;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberGradeChangeRequest {
    @NotNull(message = "변경할 등급은 필수입니다.")
    private MemberGrade grade;

    private String reason;
}
