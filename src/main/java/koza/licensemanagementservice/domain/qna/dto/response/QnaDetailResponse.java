package koza.licensemanagementservice.domain.qna.dto.response;

import koza.licensemanagementservice.domain.qna.entity.Qna;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QnaDetailResponse {
    private Long qnaId;
    private Long softwareId;
    private String softwareName;
    private String nickname;
    private String title;
    private String content;
    private QnaStatus status;
    private String answer;
    private LocalDateTime answeredAt;
    private LocalDateTime createAt;

    public static QnaDetailResponse from(Qna question) {
        return QnaDetailResponse.builder()
                .qnaId(question.getId())
                .softwareId(question.getSoftware().getId())
                .softwareName(question.getSoftware().getName())
                .nickname(question.getNickname())
                .title(question.getTitle())
                .content(question.getContent())
                .status(question.getStatus())
                .answer(question.getAnswer())
                .answeredAt(question.getAnsweredAt())
                .createAt(question.getCreateAt())
                .build();
    }
}
