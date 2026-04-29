package koza.licensemanagementservice.sdk.dto.request;

import lombok.Getter;

@Getter
public class HeartbeatRequest {
    private String sessionId;
    private String receivedSig;
    private Long receivedTs;
}
