package koza.licensemanagementservice.domain.software.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SoftwareUnsupportedRequest {
    private String reason;
}
