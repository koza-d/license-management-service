package koza.licensemanagementservice.verification.security;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ECDHExchange {
    /**
     * 서버용 ECDH 키쌍 생성
     * 매 /verify 요청마다 새로 생성
     */
    public static KeyPair generateServerKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        keyGen.initialize(new ECGenParameterSpec("secp256r1")); // P-256, SDK와 동일한 곡선
        return keyGen.generateKeyPair();
    }

    /**
     * 공유비밀키 계산
     * 서버 개인키 + SDK 공개키(Base64) -> 공유비밀키 bytes
     */
    public static byte[] computeSharedSecret(PrivateKey serverPrivateKey,
                                             String clientPublicKeyBase64) throws Exception {
        // SDK에서 받은 공개키 Base64 → PublicKey 객체로 변환
        byte[] clientKeyBytes = Base64.getDecoder().decode(clientPublicKeyBase64);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey clientPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(clientKeyBytes));

        // ECDH 공유비밀키 계산
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(serverPrivateKey);
        keyAgreement.doPhase(clientPublicKey, true);

        return keyAgreement.generateSecret(); // 이게 공유비밀키 (양쪽이 동일한 값)
    }

    /**
     * 서버 공개키 → Base64 문자열 (SDK에 전달할 값)
     */
    public static String exportPublicKey(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
