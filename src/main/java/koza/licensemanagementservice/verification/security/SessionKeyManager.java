package koza.licensemanagementservice.verification.security;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class SessionKeyManager {
    /**
     * 랜덤 sessionKey 생성 (32 bytes = AES-256)
     */
    public static byte[] generateSessionKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return key;
    }

    /**
     * sessionKey를 공유비밀키로 AES-256-GCM 암호화
     * → SDK에 전달할 encryptedSessionKey (Base64)
     */
    public static String encryptSessionKey(byte[] sessionKey,
                                           byte[] sharedSecret) throws Exception {
        // 공유비밀키 앞 32바이트를 AES 키로 사용
        SecretKeySpec aesKey = new SecretKeySpec(sharedSecret, 0, 32, "AES");

        // IV는 매번 랜덤 생성 (GCM 표준: 12 bytes)
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(sessionKey);

        // IV + 암호문을 합쳐서 Base64로 전달 (SDK가 IV를 알아야 복호화 가능)
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * sessionKey → signingKey / encryptKey 분리
     * 앞 16바이트 = signingKey (HMAC용)
     * 뒤 16바이트 = encryptKey (AES용)
     */
    public static byte[] deriveSigningKey(byte[] sessionKey) {
        byte[] key = new byte[16];
        System.arraycopy(sessionKey, 0, key, 0, 16);
        return key;
    }

    public static byte[] deriveEncryptKey(byte[] sessionKey) {
        byte[] key = new byte[16];
        System.arraycopy(sessionKey, 16, key, 0, 16);
        return key;
    }
}
