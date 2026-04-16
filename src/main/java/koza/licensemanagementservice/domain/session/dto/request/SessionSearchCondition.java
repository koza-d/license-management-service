package koza.licensemanagementservice.domain.session.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class SessionSearchCondition {
    private String q;
    private String ownerEmail;
    private String softwareName;
    private String licenseKey;
    private String licenseName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startedAfter;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startedBefore;

    public boolean hasAnyFieldFilter() {
        return ownerEmail != null || softwareName != null
                || licenseKey != null || licenseName != null;
    }

    public boolean hasFullTextFilter() {
        return q != null && !q.isBlank();
    }
}
