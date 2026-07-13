package koza.licensemanagementservice.sdk.security;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AESEncryption {
    private static final ThreadLocal<Cipher> GCM_CIPHER = ThreadLocal.withInitial(() -> {
        try {
            return Cipher.getInstance("AES/GCM/NoPadding");
        } catch (Exception e) {
            throw new IllegalStateException("AES/GCM/NoPadding Cipher 생성 실패", e);
        }
    });

    /**
     * 데이터 암호화
     * @param serverSeq 는 한 세션 동안에 절대 중첩되면 안되는 수 (nonce 추출용)
     * encryptKey로 plainText를 AES-256-GCM 암호화 → Base64 반환
     */
    public static String encrypt(Long serverSeq, String plainText, byte[] encryptKey) throws Exception {
        return encrypt(serverSeq, plainText.getBytes(StandardCharsets.UTF_8), encryptKey);
    }

    /**
     * 데이터 암호화
     * @param serverSeq 는 한 세션 동안에 절대 중첩되면 안되는 수 (nonce 추출용)
     * encryptKey로 data를 AES-256-GCM 암호화 → Base64 반환
     */
    public static String encrypt(Long serverSeq, byte[] data, byte[] encryptKey) throws Exception {
        SecretKeySpec aesKey = new SecretKeySpec(encryptKey, "AES");
        byte[] nonce = seqToNonce(serverSeq);

        Cipher cipher = GCM_CIPHER.get();
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, nonce));
        byte[] encrypted = cipher.doFinal(data);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 데이터 복호화
     */
    public static String decrypt(Long clientSeq, String encryptedBase64, byte[] encryptKey) throws Exception {
        byte[] encrypted = Base64.getDecoder().decode(encryptedBase64); // IllegalArgumentException
        byte[] nonce = seqToNonce(clientSeq);

        SecretKeySpec aesKey = new SecretKeySpec(encryptKey, "AES");
        Cipher cipher = GCM_CIPHER.get();
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, nonce));

        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }

    private static byte[] seqToNonce(long seq) {
        return ByteBuffer.allocate(12) // 12바이트 (앞 4바이트 0)
                .position(4) // 뒤 8바이트에 seq
                .putLong(seq)
                .array();
    }
}
