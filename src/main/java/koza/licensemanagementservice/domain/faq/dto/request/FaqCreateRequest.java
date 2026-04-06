package koza.licensemanagementservice.domain.faq.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FaqCreateRequest {
    @Schema(description = "FAQ 카테고리", example = "설치")
    private String category;

    @NotBlank(message = "질문은 필수 입력 값입니다.")
    @Schema(description = "FAQ 질문", example = "프로그램 설치는 어떻게 하나요?")
    private String question;

    @NotBlank(message = "답변은 필수 입력 값입니다.")
    @Schema(description = "FAQ 답변", example = "다운로드 페이지에서 설치파일을 받으세요.")
    private String answer;

    @Schema(description = "정렬 순서", example = "0")
    private Integer sortOrder;
}
