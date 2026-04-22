package koza.licensemanagementservice.domain.qna.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QnaAnswerRequest {
    @NotBlank(message = "답변 내용은 필수입니다.")
    @Size(max = 2000, message = "답변은 2000자를 넘을 수 없습니다.")
    @Schema(description = "답변 내용", example = "안녕하세요, 확인 후 조치하겠습니다.")
    private String answer;
}
