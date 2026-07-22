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
    private String appId;
    private Map<String, String> globalVariables;
    private Map<String, String> localVariables;
    public static SoftwareCreateResponse of(Software software, String latestVersion) {

        return SoftwareCreateResponse.builder()
                .id(software.getId())
                .name(software.getName())
                .latestVersion(latestVersion)
                .appId(software.getAppId())
                .globalVariables(software.getGlobalVariables())
                .localVariables(software.getLocalVariables())
                .build();
    }
}
