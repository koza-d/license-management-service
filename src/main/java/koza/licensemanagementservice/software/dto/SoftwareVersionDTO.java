package koza.licensemanagementservice.software.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import koza.licensemanagementservice.software.entity.SoftwareVersion;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.LocalDateTime;

public class SoftwareVersionDTO {
    @Getter
    public static class CreateRequest {
        @NotNull(message = "소프트웨어 정보가 없습니다.")
        @Schema(description = "소프트웨어 고유 ID", example = "1")
        private Long softwareId;

        @NotBlank
        @Pattern(
                regexp = "^[0-9]+\\.[0-9]+\\.[0-9]+$",
                message = "버전 형식은 1.0.0과 같아야 합니다."
        )
        @Schema(description = "소프트웨어 버전(ex. 1.0.0)", example = "1.0.0")
        private String version;

        @Size(min = 64, max = 64, message = "해시값은 64자여야 합니다.")
        @Pattern(regexp = "^[a-fA-F0-9]{64}$", message = "올바른 SHA-256 해시 형식이 아닙니다.")
        private String fileHash;

        @NotNull(message = "사용가능 여부는 필수입니다.")
        private boolean isAvailable;

        @Size(max = 2048, message = "다운로드 URL의 최대 길이는 2048자 입니다.")
        @Pattern(regexp = "^$|^(https?|ftp)://.*$", message = "올바른 URL 형식이 아닙니다.")
        private String downloadURL;

        @Size(max = 200, message = "메모란의 최대 길이는 200자 입니다.")
        private String memo;
    }

    @Getter
    public static class UpdateRequest {
        @NotBlank
        @Pattern(
                regexp = "^[0-9]+\\.[0-9]+\\.[0-9]+$",
                message = "버전 형식은 1.0.0과 같아야 합니다."
        )
        @Schema(description = "소프트웨어 버전(ex. 1.0.0)", example = "1.0.0")
        private String version;

        @Size(min = 64, max = 64, message = "해시값은 64자여야 합니다.")
        @Pattern(regexp = "^[a-fA-F0-9]{64}$", message = "올바른 SHA-256 해시 형식이 아닙니다.")
        private String fileHash;

        private Boolean available;

        @Size(max = 2048, message = "다운로드 URL의 최대 길이는 2048자 입니다.")
        @Pattern(regexp = "^$|^(https?|ftp)://.*$", message = "올바른 URL 형식이 아닙니다.")
        private String downloadURL;

        @Size(max = 200, message = "메모란의 최대 길이는 200자 입니다.")
        private String memo;
    }

    @Getter
    @Builder
    public static class SummaryResponse implements Comparable<SummaryResponse> {
        private Long versionId;
        private String version;
        private boolean isAvailable;
        private String memo;
        private LocalDateTime createAt;
        private LocalDateTime updateAt;

        public static SummaryResponse from(SoftwareVersion version) {
            return SummaryResponse.builder()
                    .versionId(version.getId())
                    .version(version.getVersion())
                    .isAvailable(version.isAvailable())
                    .memo(version.getMemo())
                    .createAt(version.getCreateAt())
                    .updateAt(version.getUpdateAt())
                    .build();
        }

        @Override
        public int compareTo(SummaryResponse o) {
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

    @Getter
    @Builder
    public static class DetailResponse {
        private Long versionId;
        private String version;
        private String fileHash;
        private boolean isAvailable;
        private String downloadURL;
        private String memo;
        private LocalDateTime createAt;
        private LocalDateTime updateAt;

        public static DetailResponse from(SoftwareVersion version) {
            return DetailResponse.builder()
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
}
