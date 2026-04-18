package koza.licensemanagementservice.domain.qna.dto.response;

import koza.licensemanagementservice.domain.qna.entity.Qna;
import koza.licensemanagementservice.domain.qna.entity.QnaPriority;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class QnaListResponse {
    private Long qnaId;
    private String softwareName;
    private String nickname;
    private String title;
    private QnaStatus status;
    private QnaPriority priority;
    private LocalDateTime createAt;

    public static QnaListResponse from(Qna question) {
        return QnaListResponse.builder()
                .qnaId(question.getId())
                .softwareName(question.getSoftware().getName())
                .nickname(question.getNickname())
                .title(question.getTitle())
                .status(question.getStatus())
                .priority(question.getPriority())
                .createAt(question.getCreateAt())
                .build();
    }
}
