package koza.licensemanagementservice.verification.dto.request;

import lombok.Getter;

@Getter
public class VerifyRequest {
    private String publicKey;
    private String licenseKey;
}
