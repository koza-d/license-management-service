package koza.licensemanagementservice.domain.faq.dto.response;

import koza.licensemanagementservice.domain.faq.entity.Faq;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FaqResponse {
    private Long id;
    private String category;
    private String question;
    private String answer;
    private int sortOrder;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public static FaqResponse from(Faq faq) {
        return FaqResponse.builder()
                .id(faq.getId())
                .category(faq.getCategory())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .sortOrder(faq.getSortOrder())
                .createAt(faq.getCreateAt())
                .updateAt(faq.getUpdateAt())
                .build();
    }
}
