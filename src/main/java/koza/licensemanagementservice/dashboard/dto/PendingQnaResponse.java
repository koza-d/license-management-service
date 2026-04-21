package koza.licensemanagementservice.dashboard.dto;

import com.querydsl.core.annotations.QueryProjection;
import koza.licensemanagementservice.domain.qna.entity.QnaPriority;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PendingQnaResponse {
    private final Long qnaId;
    private final String title;
    private final String softwareName;
    private final String writerEmail;
    private final QnaPriority priority;
    private final LocalDateTime createdAt;

    @QueryProjection
    public PendingQnaResponse(Long qnaId,
                              String title,
                              String softwareName,
                              String writerEmail,
                              QnaPriority priority,
                              LocalDateTime createdAt) {
        this.qnaId = qnaId;
        this.title = title;
        this.softwareName = softwareName;
        this.writerEmail = writerEmail;
        this.priority = priority;
        this.createdAt = createdAt;
    }
}
