package koza.licensemanagementservice.domain.audit.dto.condition;

import koza.licensemanagementservice.domain.audit.entity.EventCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditSearchCondition {
    private Set<EventCategory> category;
    private String eventType;
    private String actorEmail;
    private String targetType;
    private Long targetId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

    private String q;

    public boolean hasAnyFieldFilter() {
        return eventType != null
                || (actorEmail != null && !actorEmail.isBlank())
                || (targetType != null && targetId != null);
    }

    public boolean hasFullTextFilter() {
        return q != null && !q.isBlank();
    }
}
