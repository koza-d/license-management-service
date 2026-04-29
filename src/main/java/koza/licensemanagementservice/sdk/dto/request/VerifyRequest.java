package koza.licensemanagementservice.sdk.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyRequest {
    private String publicKey;
    private String licenseKey;
    private String appId;
    private String clientVersion;
    private String fileHash;
}
