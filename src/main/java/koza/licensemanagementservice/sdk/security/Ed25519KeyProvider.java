package koza.licensemanagementservice.sdk.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;

@Component
@Getter
public class Ed25519KeyProvider {

    @Value("${sdk.signing.ed25519-private-key}")
    private String privateKeyBase64;

    private PrivateKey privateKey;

    @Value("${sdk.signing.ed25519-key-id}")
    private int keyId;

    @PostConstruct
    public void init() throws Exception {
        this.privateKey = Ed25519Signer.loadPrivateKey(privateKeyBase64);
        this.keyId = 1;
    }
}
