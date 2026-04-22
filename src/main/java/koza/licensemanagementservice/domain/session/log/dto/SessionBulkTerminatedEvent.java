package koza.licensemanagementservice.domain.session.log.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SessionBulkTerminatedEvent {
    private Long operatorId;
    private List<String> sessionIds;
    private int terminated;
    private int failed;
    private String reason;
}
