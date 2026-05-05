package koza.licensemanagementservice.domain.session.dto.condition;

import koza.licensemanagementservice.domain.session.repository.SessionSearchTarget;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class SessionSearchCondition {
    private SessionSearchTarget target;
    private String search;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;
}
