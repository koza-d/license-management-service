package koza.licensemanagementservice.domain.software.version.dto;

import koza.licensemanagementservice.domain.software.version.entity.SoftwareVersion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SoftwareVersionDetailResponse {
    private Long versionId;
    private String version;
    private String fileHash;
    private boolean isLatest;
    private boolean isAvailable;
    private String downloadURL;
    private String memo;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public static SoftwareVersionDetailResponse of(SoftwareVersion version, Long latestVersionId) {
        return SoftwareVersionDetailResponse.builder()
                .versionId(version.getId())
                .version(version.getVersion())
                .fileHash(version.getFileHash())
                .isLatest(latestVersionId == null ? false : version.getId().equals(latestVersionId))
                .isAvailable(version.isAvailable())
                .downloadURL(version.getDownloadURL())
                .memo(version.getMemo())
                .createAt(version.getCreateAt())
                .updateAt(version.getUpdateAt())
                .build();
    }
}
