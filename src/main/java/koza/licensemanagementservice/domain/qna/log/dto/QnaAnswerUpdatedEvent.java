package koza.licensemanagementservice.domain.qna.log.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QnaAnswerUpdatedEvent {
    private Long operatorId;
    private Long qnaId;
    private String qnaTitle;
    private Long askerId;
    private String askerEmail;
    private String beforeAnswer;
    private String afterAnswer;
}
