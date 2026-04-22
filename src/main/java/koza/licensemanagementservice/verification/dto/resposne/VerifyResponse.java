package koza.licensemanagementservice.verification.dto.resposne;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class VerifyResponse {
    private String serverPublicKey;
    private String encryptedSessionKey;
    private String encryptedData; // VerifyData 직렬화 -> 암호화한 문자열
    private String sig;
    private String ts;
}
