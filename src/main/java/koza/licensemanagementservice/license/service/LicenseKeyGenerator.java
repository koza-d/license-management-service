package koza.licensemanagementservice.license.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
public class LicenseKeyGenerator {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; //. I, O, 1, 0은 제외(오타방지)
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateKey() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            if (i > 0 && i % 4 == 0) sb.append("-"); // 4자리마다 하이픈
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
