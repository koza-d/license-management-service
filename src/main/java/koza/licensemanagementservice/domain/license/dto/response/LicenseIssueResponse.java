package koza.licensemanagementservice.domain.license.dto.response;

import koza.licensemanagementservice.domain.license.entity.License;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LicenseIssueResponse {
    private Long id;
    private String softwareName;
    private String licenseName;
    private String memo;
    private String licenseKey;
    private LocalDateTime expiredAt;
    private LocalDateTime createAt;

    public static LicenseIssueResponse from(License license) {
        return LicenseIssueResponse.builder()
                .id(license.getId())
                .softwareName(license.getSoftware().getName())
                .licenseName(license.getName())
                .memo(license.getMemo())
                .licenseKey(license.getLicenseKey())
                .expiredAt(license.getExpiredAt())
                .createAt(license.getCreateAt())
                .build();
    }
}
