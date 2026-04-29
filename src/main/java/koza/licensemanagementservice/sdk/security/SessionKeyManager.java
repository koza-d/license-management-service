package koza.licensemanagementservice.sdk.security;

import java.security.SecureRandom;

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
