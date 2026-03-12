package koza.licensemanagementservice.domain.software.dto.response;

import koza.licensemanagementservice.domain.software.entity.Software;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class SoftwareCreateResponse {
    private Long id;
    private String name;
    private String latestVersion;
    private String apiKey;
    private int limitLicense;
    private Map<String, Object> globalVariables;
    private Map<String, Object> localVariables;
    public static SoftwareCreateResponse from(Software software) {
        return SoftwareCreateResponse.builder()
                .id(software.getId())
                .name(software.getName())
                .latestVersion(software.getLatestVersion())
                .apiKey(software.getApiKey())
                .limitLicense(software.getLimitLicense())
                .globalVariables(software.getGlobalVariables())
                .localVariables(software.getLocalVariables())
                .build();
    }
}
