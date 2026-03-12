package koza.licensemanagementservice.domain.software.dto.response;

import koza.licensemanagementservice.domain.software.entity.Software;
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
    private String apiKey;
    private int licenseCount;
    private int limitLicense;
    private int remainLicense;
    private Map<String, Object> globalVariables;
    private Map<String, Object> localVariables;
    private LocalDateTime createAt;

    public static SoftwareDetailResponse of(Software software, int licenseCount) {
        return SoftwareDetailResponse.builder()
                .id(software.getId())
                .name(software.getName())
                .latestVersion(software.getLatestVersion())
                .apiKey(software.getApiKey())
                .licenseCount(licenseCount)
                .limitLicense(software.getLimitLicense())
                .remainLicense(software.getLimitLicense() - licenseCount)
                .globalVariables(software.getGlobalVariables())
                .localVariables(software.getLocalVariables())
                .createAt(software.getCreateAt())
                .build();
    }
}
