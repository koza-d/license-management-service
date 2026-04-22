package koza.licensemanagementservice.domain.software.version.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SoftwareVersionCreateRequest {
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
