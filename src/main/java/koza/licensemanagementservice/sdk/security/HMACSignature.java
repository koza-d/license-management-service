package koza.licensemanagementservice.sdk.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class HMACSignature {
    /**
     * 서명 생성
     * signingKey + data → HMAC-SHA256 → Base64
     */
    public static String sign(String data, byte[] signingKey) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(signingKey, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 서명 검증
     * 받은 sig와 직접 계산한 sig가 일치하면 진짜 요청
     */
    public static boolean verify(String data, String receivedSig,
                                 byte[] signingKey) throws Exception {
        String expectedSig = sign(data, signingKey);
        // 타이밍 공격 방지를 위해 MessageDigest.isEqual 사용 (단순 equals 금지)
        return MessageDigest.isEqual(
                Base64.getDecoder().decode(expectedSig),
                Base64.getDecoder().decode(receivedSig)
        );
    }
}
