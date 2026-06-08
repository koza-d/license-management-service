package koza.licensemanagementservice.sdk.dto.resposne;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyResponse {
    private String serverPublicKey;
    private String clientNonce; // SDK에서 보내는 Nonce (replay 방지)
    private int keyId; // Ed25519 키 회전용(어떤 키를 써야할지 클라이언트에게 알림)
    private String encryptedData; // VerifyData 직렬화 -> 암호화한 문자열
    private String serverSign; // 서버 신원 서명 (serverPublicKey, clientNonce, keyId, encryptedData)
}
