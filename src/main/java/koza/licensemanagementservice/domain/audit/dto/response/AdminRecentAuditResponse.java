package koza.licensemanagementservice.domain.audit.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import koza.licensemanagementservice.domain.audit.entity.EventCategory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminRecentAuditResponse {
    private final Long id;
    private final EventCategory eventCategory;
    private final String summary;
    private final String actorEmail;
    private final LocalDateTime createdAt;

    @QueryProjection
    public AdminRecentAuditResponse(Long id,
                                    EventCategory eventCategory,
                                    String summary,
                                    String actorEmail,
                                    LocalDateTime createdAt) {
        this.id = id;
        this.eventCategory = eventCategory;
        this.summary = summary;
        this.actorEmail = actorEmail;
        this.createdAt = createdAt;
    }
}
