package koza.licensemanagementservice.domain.qna.dto.response;

import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class QnaAdminListResponse {
    private Long qnaId;
    private String title;
    private String memberEmail;
    private String softwareName;
    private QnaStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;
}
