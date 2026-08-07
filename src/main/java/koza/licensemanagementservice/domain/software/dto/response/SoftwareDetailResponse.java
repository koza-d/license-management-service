package koza.licensemanagementservice.domain.software.dto.response;

import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import koza.licensemanagementservice.domain.software.version.entity.SoftwareVersion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class SoftwareDetailResponse {
    private Long id;
    private String name;
    private String latestVersion;
    private SoftwareStatus status;
    private String appId;
    private LocalDateTime statusUntil;
    private String statusReason;
    private int licenseCount;
    private Map<String, String> globalVariables;
    private Map<String, String> localVariables;
    private LocalDateTime createAt;

    public static SoftwareDetailResponse of(Software software, int licenseCount) {
        SoftwareVersion latestVersion = software.getLatestVersion();

        return SoftwareDetailResponse.builder()
                .id(software.getId())
                .name(software.getName())
                .latestVersion(latestVersion != null ? latestVersion.getVersion() : "최신버전 찾을 수 없음")
                .status(software.getStatus())
                .appId(software.getAppId())
                .statusUntil(software.getStatusUntil())
                .statusReason(software.getStatusReason())
                .licenseCount(licenseCount)
                .globalVariables(software.getGlobalVariables())
                .localVariables(software.getLocalVariables())
                .createAt(software.getCreateAt())
                .build();
    }
}
