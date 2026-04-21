package koza.licensemanagementservice.domain.software.service;

import java.security.SecureRandom;
import java.util.Base64;

public class SoftwareKeyGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final String APP_ID_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";

    public static String generateApiKey() {
        // 64바이트의 난수 생성 (인코딩 후 약 88자)
        byte[] randomBytes = new byte[64];
        RANDOM.nextBytes(randomBytes);

        // URL에 안전한 문자로 인코딩 (/, + 대신 _, - 사용)
        return ENCODER.encodeToString(randomBytes);
    }

    public static String generateAppId() {
        int length = 10;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(APP_ID_CHARACTERS.charAt(RANDOM.nextInt(APP_ID_CHARACTERS.length())));
        }
        return sb.toString();
    }
}
