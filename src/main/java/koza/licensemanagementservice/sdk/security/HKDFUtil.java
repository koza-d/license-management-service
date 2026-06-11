package koza.licensemanagementservice.sdk.security;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HKDFUtil {

    /**
     * 공유비밀 → 세션키 도출
     */
    public static byte[] derive(byte[] sharedSecret, byte[] salt, String info, int length) {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(sharedSecret, salt, info.getBytes(StandardCharsets.UTF_8)));
        byte[] out = new byte[length];
        hkdf.generateBytes(out, 0, length);
        return out;
    }

    /** 방향별 키 한 번에 (AES-256 → 32바이트) */
    public static SessionKeys deriveSessionKeys(byte[] sharedSecret, byte[] salt) {
        byte[] c2s = derive(sharedSecret, salt, "client-to-server", 32);
        byte[] s2c = derive(sharedSecret, salt, "server-to-client", 32);
        return new SessionKeys(c2s, s2c);
    }

    public record SessionKeys(byte[] keyC2S, byte[] keyS2C) {}
}