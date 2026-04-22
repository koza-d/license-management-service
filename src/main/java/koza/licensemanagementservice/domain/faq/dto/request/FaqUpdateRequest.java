package koza.licensemanagementservice.domain.faq.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class FaqUpdateRequest {
    @Schema(description = "FAQ 카테고리", example = "설치")
    private String category;

    @Schema(description = "FAQ 질문", example = "프로그램 설치는 어떻게 하나요?")
    private String question;

    @Schema(description = "FAQ 답변", example = "다운로드 페이지에서 설치파일을 받으세요.")
    private String answer;

    @Schema(description = "정렬 순서", example = "1")
    private Integer sortOrder;
}
