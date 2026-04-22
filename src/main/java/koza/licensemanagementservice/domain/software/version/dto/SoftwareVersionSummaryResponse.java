package koza.licensemanagementservice.domain.software.version.dto;

import koza.licensemanagementservice.domain.software.version.entity.SoftwareVersion;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.LocalDateTime;

@Getter
@Builder
public class SoftwareVersionSummaryResponse implements Comparable<SoftwareVersionSummaryResponse>  {
    private Long versionId;
    private String version;
    private String fileHash;
    private boolean isAvailable;
    private String downloadURL;
    private String memo;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public static SoftwareVersionSummaryResponse from(SoftwareVersion version) {
        return SoftwareVersionSummaryResponse.builder()
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

    @Override
    public int compareTo(SoftwareVersionSummaryResponse o) {
        String[] vs1 = version.split("\\.");
        String[] vs2 = o.getVersion().split("\\.");
        int i = 0;

        while (i < vs1.length || i < vs2.length) {
            int n1 = i < vs1.length ? NumberUtils.toInt(vs1[i]) : 0;
            int n2 = i < vs2.length ? NumberUtils.toInt(vs2[i]) : 0;
            if (n1 != n2) {
                return Integer.compare(n1, n2);
            }
            i++;
        }
        return 0;
    }

}
