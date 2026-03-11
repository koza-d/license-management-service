package koza.licensemanagementservice.license.dto.response;

import koza.licensemanagementservice.license.entity.License;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Builder
public class LicenseSummaryResponse {
    private Long licenseId;
    private String licenseName;
    private String licenseKey;
    private String memo;
    private LocalDateTime createAt;
    private LocalDateTime expiredAt;
    private boolean hasActiveSession;
    private LocalDateTime latestActiveAt;
    private String status;

    public static LicenseSummaryResponse of(License license, LocalDateTime latestActiveAt) {
        long remainingMs = calcRemainingMs(license.getExpiredAt());
        return LicenseSummaryResponse.builder()
                .licenseId(license.getId())
                .licenseName(license.getName())
                .licenseKey(maskLicenseKey(license.getLicenseKey())) // 첫 4자리 제외 마스킹(-제외)
                .memo(license.getMemo())
                .createAt(license.getCreateAt())
                .expiredAt(license.getExpiredAt())
                .hasActiveSession(license.hasActiveSession())
                .latestActiveAt(latestActiveAt)
                .status(license.getStatus().name())
                .build();
    }

    private static Long calcRemainingMs(LocalDateTime expiredAt) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, expiredAt);
        return Math.max(0, duration.toMillis());
    }

    private static String maskLicenseKey(String licenseKey) {
        if (licenseKey == null || licenseKey.length() <= 4) return licenseKey;

        // 앞 4자리를 제외한 나머지를 *로 채우되, 하이픈 위치는 유지
        char[] chars = licenseKey.toCharArray();
        for (int i = 4; i < chars.length; i++) {
            if (chars[i] != '-') {
                chars[i] = '*';
            }
        }
        return new String(chars);
    }
}
