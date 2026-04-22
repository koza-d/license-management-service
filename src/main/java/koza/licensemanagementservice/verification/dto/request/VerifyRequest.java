package koza.licensemanagementservice.verification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyRequest {
    private String publicKey;
    private String licenseKey;
    private String appId;
}
