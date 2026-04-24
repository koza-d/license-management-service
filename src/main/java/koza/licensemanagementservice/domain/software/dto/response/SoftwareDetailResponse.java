package koza.licensemanagementservice.domain.software.dto.response;

import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import koza.licensemanagementservice.domain.software.version.entity.SoftwareVersion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Getter
@Builder
public class SoftwareDetailResponse {
    private Long id;
    private String name;
    private String latestVersion;
    private SoftwareStatus status;
    private String appId;
    private int licenseCount;
    private int limitLicense;
    private int remainLicense;
    private Map<String, Object> globalVariables;
    private Map<String, Object> localVariables;
    private LocalDateTime createAt;

    public static SoftwareDetailResponse of(Software software, int licenseCount) {
        Optional<SoftwareVersion> latestVersion = software.getVersions().stream()
                .filter(SoftwareVersion::isLatest)
                .findAny();

        return SoftwareDetailResponse.builder()
                .id(software.getId())
                .name(software.getName())
                .latestVersion(latestVersion.isEmpty() ? "최신버전 찾을 수 없음" : latestVersion.get().getVersion())
                .status(software.getStatus())
                .appId(software.getAppId())
                .licenseCount(licenseCount)
                .limitLicense(software.getLimitLicense())
                .remainLicense(software.getLimitLicense() - licenseCount)
                .globalVariables(software.getGlobalVariables())
                .localVariables(software.getLocalVariables())
                .createAt(software.getCreateAt())
                .build();
    }
}
