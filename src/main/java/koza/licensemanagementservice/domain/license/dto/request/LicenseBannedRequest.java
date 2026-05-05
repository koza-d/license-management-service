package koza.licensemanagementservice.domain.license.dto.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LicenseBannedRequest {
    private int days;
    private String reason;
}
