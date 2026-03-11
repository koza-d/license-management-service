package koza.licensemanagementservice.license.dto.response;

import koza.licensemanagementservice.license.entity.License;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Builder
public class LicenseExtendResponse {
    private String name;
    private String licenseKey;
    private String memo;
    private LocalDateTime expireAt; // 만료일자
    private long remainingMs; // 연장하고 남은 ms
    private int extendDays; // 연장 일 수
    private String status; // 라이센스 상태

    public static LicenseExtendResponse of(License license, int extendDays) {
        long remainingMs = calcRemainingMs(license.getExpiredAt());

        return LicenseExtendResponse.builder()
                .name(license.getName())
                .licenseKey(license.getLicenseKey())
                .memo(license.getMemo())
                .expireAt(license.getExpiredAt())
                .remainingMs(remainingMs)
                .extendDays(extendDays)
                .status(license.getStatus().name())
                .build();
    }

    private static Long calcRemainingMs(LocalDateTime expiredAt) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, expiredAt);
        return Math.max(0, duration.toMillis());
    }
}
