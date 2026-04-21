package koza.licensemanagementservice.domain.qna.log.dto;

import koza.licensemanagementservice.domain.qna.entity.QnaPriority;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QnaPriorityChangedEvent {
    private Long operatorId;
    private Long qnaId;
    private String qnaTitle;
    private QnaPriority before;
    private QnaPriority after;
}
