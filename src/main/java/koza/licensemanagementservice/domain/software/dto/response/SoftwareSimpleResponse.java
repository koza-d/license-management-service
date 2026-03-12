package koza.licensemanagementservice.domain.software.dto.response;

import koza.licensemanagementservice.domain.software.entity.Software;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SoftwareSimpleResponse {
    private Long softwareId;
    private String softwareName;

    public static SoftwareSimpleResponse of(Software software) {
        return SoftwareSimpleResponse.builder()
                .softwareId(software.getId())
                .softwareName(software.getName())
                .build();
    }
}
