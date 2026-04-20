package koza.licensemanagementservice.domain.audit.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import koza.licensemanagementservice.domain.audit.entity.EventCategory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AuditLogResponse {
    private final Long id;
    private final EventCategory eventCategory;
    private final String eventType;
    private final String actorEmail;
    private final String targetType;
    private final Long targetId;
    private final String targetLabel;
    private final String summary;
    private final LocalDateTime createdAt;

    @QueryProjection
    public AuditLogResponse(Long id,
                            EventCategory eventCategory,
                            String eventType,
                            String actorEmail,
                            String targetType,
                            Long targetId,
                            String targetLabel,
                            String summary,
                            LocalDateTime createdAt) {
        this.id = id;
        this.eventCategory = eventCategory;
        this.eventType = eventType;
        this.actorEmail = actorEmail;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetLabel = targetLabel;
        this.summary = summary;
        this.createdAt = createdAt;
    }
}
