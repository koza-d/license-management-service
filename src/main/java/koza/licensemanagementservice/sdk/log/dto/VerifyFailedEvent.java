package koza.licensemanagementservice.sdk.log.dto;

import lombok.Data;

@Data
public class VerifyFailedEvent {
    private final Long softwareId;
    private final String appId;
    private final Long licenseId;
    private final String licenseKey;
    private final String failCode;
    private final String ipAddress;
    private final String userAgent;
}
