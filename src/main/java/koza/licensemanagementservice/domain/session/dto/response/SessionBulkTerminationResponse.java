package koza.licensemanagementservice.domain.session.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SessionBulkTerminationResponse {
    private int terminated;
    private int failed;
}
