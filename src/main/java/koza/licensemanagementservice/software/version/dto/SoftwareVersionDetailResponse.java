package koza.licensemanagementservice.software.version.dto;

import koza.licensemanagementservice.software.version.entity.SoftwareVersion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SoftwareVersionDetailResponse {
    private Long versionId;
    private String version;
    private String fileHash;
    private boolean isAvailable;
    private String downloadURL;
    private String memo;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public static SoftwareVersionDetailResponse from(SoftwareVersion version) {
        return SoftwareVersionDetailResponse.builder()
                .versionId(version.getId())
                .version(version.getVersion())
                .fileHash(version.getFileHash())
                .isAvailable(version.isAvailable())
                .downloadURL(version.getDownloadURL())
                .memo(version.getMemo())
                .createAt(version.getCreateAt())
                .updateAt(version.getUpdateAt())
                .build();
    }
}
