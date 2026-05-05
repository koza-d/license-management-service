package koza.licensemanagementservice.stat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionPeakResponse {
    private final int unit;
    private final double avg;
    private final double max;
}
