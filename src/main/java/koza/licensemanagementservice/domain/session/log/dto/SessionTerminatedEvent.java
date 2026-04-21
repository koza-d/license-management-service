package koza.licensemanagementservice.domain.session.log.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionTerminatedEvent {
    private Long operatorId;
    private String sessionId;
    private Long licenseId;
    private String reason;
}
