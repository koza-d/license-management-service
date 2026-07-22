package koza.licensemanagementservice.sdk.security;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ECDHExchange {
    /**
     * 서버용 ECDH 키쌍 생성(X25519)
     * 매 /verify 요청마다 새로 생성
     */
    public static KeyPair generateServerKeyPair() throws Exception {
        return KeyPairGenerator.getInstance("X25519").generateKeyPair();
    }

    /**
     * 공유비밀키 계산
     * 서버 개인키 + SDK 공개키(Base64) -> 공유비밀키(32bytes)
     */
    public static byte[] computeSharedSecret(PrivateKey serverPrivateKey,
                                             byte[] clientKeyBytes) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("X25519");
        PublicKey clientPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(clientKeyBytes));

        KeyAgreement keyAgreement = KeyAgreement.getInstance("X25519");
        keyAgreement.init(serverPrivateKey);
        keyAgreement.doPhase(clientPublicKey, true);

        return keyAgreement.generateSecret(); // 공유비밀키 (서버, 클라 양쪽이 동일한 값)
    }
}
