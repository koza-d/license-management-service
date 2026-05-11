package koza.licensemanagementservice.sdk.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InitRequest {
    private String appId;
    private String clientVersion;
    private String fileHash;
}
