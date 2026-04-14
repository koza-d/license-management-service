package koza.licensemanagementservice.domain.license.dto.response;

import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.version.entity.SoftwareVersion;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Getter
@Builder
public class LicenseAdminDetailResponse {
    private Long id;
    private Long softwareId;
    private String softwareName;
    private String softwareLatestVersion;
    private String licenseName;
    private String memo;
    private String licenseKey;
    private LocalDateTime latestActiveAt; // 마지막 라이센스 활성 시간
    private LocalDateTime expiredAt;
    private long remainingMs; // 남은시간(ms)
    private Map<String, Object> defaultVariables; // 소프트웨어에 설정된 기본 지역변수
    private Map<String, Object> modifiedVariables; // 라이센스 층에서 수정된 지역변수
    private Map<String, Object> finalVariables; // 기본값에 수정된 지역변수를 덮어씌운 결과
    private String status;
    private LocalDateTime createAt;

    public static LicenseAdminDetailResponse of(License license, LocalDateTime latestActiveAt, Map<String, Object> finalVariables) {
        long remainingMs = calcRemainingMs(license.getExpiredAt());

        Software software = license.getSoftware();
        Optional<SoftwareVersion> latestVersion = software.getVersions().stream()
                .filter(SoftwareVersion::isLatest)
                .findAny();

        return LicenseAdminDetailResponse.builder()
                .id(license.getId())
                .softwareId(software.getId())
                .softwareName(software.getName())
                .softwareLatestVersion(latestVersion.isEmpty() ? "최신버전 찾을 수 없음" : latestVersion.get().getVersion())
                .licenseName(license.getName())
                .memo(license.getMemo())
                .licenseKey(license.getLicenseKey())
                .latestActiveAt(latestActiveAt)
                .expiredAt(license.getExpiredAt())
                .remainingMs(remainingMs)
                .status(license.getStatus().name())
                .defaultVariables(software.getLocalVariables())
                .modifiedVariables(license.getRawLocalVariables())
                .finalVariables(finalVariables)
                .createAt(license.getCreateAt())
                .build();
    }

    private static Long calcRemainingMs(LocalDateTime expiredAt) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, expiredAt);
        return Math.max(0, duration.toMillis());
    }

}
