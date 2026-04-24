package koza.licensemanagementservice.domain.software.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SoftwareBanRequest {
    private final int untilDays;
    private final String reason;
}
