package koza.licensemanagementservice.sdk.log.dto;

import lombok.Data;

@Data
public class InitSuccessEvent {
    private final Long softwareId;
    private final String appId;
    private final String clientVersion;
    private final String ipAddress;
    private final String userAgent;
}
