package koza.licensemanagementservice.sdk.log.dto;

import lombok.Data;

@Data
public class InitFailedEvent {
    private final Long softwareId;
    private final String appId;
    private final String clientVersion;
    private final String failCode;
    private final String ipAddress;
    private final String userAgent;
}
