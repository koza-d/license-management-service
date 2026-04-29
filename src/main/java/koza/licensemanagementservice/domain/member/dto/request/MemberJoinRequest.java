package koza.licensemanagementservice.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberJoinRequest {
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Schema(description = "이메일", example = "test@test.com")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 최소 8자 최대 20자입니다.")
    @Schema(description = "비밀번호 8~20자", example = "11112222")
    private String password;

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 최소 2자, 최대 10자입니다.")
    @Schema(description = "닉네임 2~10자", example = "테스트")
    private String nickname;
}
