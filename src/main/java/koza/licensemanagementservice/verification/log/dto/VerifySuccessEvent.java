package koza.licensemanagementservice.verification.log.dto;

import lombok.Data;

@Data
public class VerifySuccessEvent {
    private final Long softwareId;
    private final String appId;
    private final Long licenseId;
    private final String licenseKey;
    private final String ipAddress;
    private final String userAgent;
}
