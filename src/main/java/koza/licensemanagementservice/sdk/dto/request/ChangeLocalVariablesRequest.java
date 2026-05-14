package koza.licensemanagementservice.sdk.dto.request;

import lombok.Getter;

@Getter
public class ChangeLocalVariablesRequest {
    private String sessionId;
    private String key;
    private String value;
    private String receivedSig;
}
