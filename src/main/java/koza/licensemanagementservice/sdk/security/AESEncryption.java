package koza.licensemanagementservice.sdk.security;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AESEncryption {
    /**
     * 데이터 암호화
     * encryptKey로 plainText를 AES-128-GCM 암호화 → Base64 반환
     */
    public static String encrypt(String plainText, byte[] encryptKey) throws Exception {
        return encrypt(plainText.getBytes(StandardCharsets.UTF_8), encryptKey);
    }

    /**
     * 데이터 암호화
     * encryptKey로 data를 AES-128-GCM 암호화 → Base64 반환
     */
    public static String encrypt(byte[] data, byte[] encryptKey) throws Exception {
        SecretKeySpec aesKey = new SecretKeySpec(encryptKey, "AES");

        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv); // IV 매번 랜덤 생성 (중요!)

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(data);

        // IV + 암호문 합쳐서 반환
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * 데이터 복호화 (서버에서 검증용으로 필요할 경우)
     */
    public static String decrypt(String encryptedBase64, byte[] encryptKey) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedBase64);

        // IV(앞 12바이트)와 암호문 분리
        byte[] iv = new byte[12];
        byte[] encrypted = new byte[combined.length - 12];
        System.arraycopy(combined, 0, iv, 0, 12);
        System.arraycopy(combined, 12, encrypted, 0, encrypted.length);

        SecretKeySpec aesKey = new SecretKeySpec(encryptKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));

        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }
}
