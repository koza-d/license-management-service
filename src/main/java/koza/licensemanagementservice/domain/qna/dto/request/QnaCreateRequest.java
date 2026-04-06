package koza.licensemanagementservice.domain.qna.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QnaCreateRequest {
    @NotNull(message = "소프트웨어 ID는 필수입니다.")
    @Schema(description = "소프트웨어 ID", example = "10")
    private Long softwareId;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(min = 2, max = 100, message = "제목은 2~100자입니다.")
    @Schema(description = "문의 제목", example = "라이센스 오류 문의")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "문의 내용", example = "라이센스 키 입력 시 오류가 발생합니다...")
    private String content;
}
