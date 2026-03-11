package koza.licensemanagementservice.license.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class LicenseExtendRequest {
    @NotNull(message = "소프트웨어 고유ID는 필수 값입니다.")
    @Schema(description = "소프트웨어 고유 ID", example = "1")
    private Long softwareId;

    @NotNull(message = "라이센스 ID는 최소 1개가 필요합니다.")
    @Schema(description = "라이센스 ID 리스트", example = "[1, 2]")
    private List<Long> ids; // 라이센스 id 리스트

    @NotNull(message = "연장일 설정은 필수입니다.")
    @Schema(description = "연장 일(일단위)", example = "30")
    private int days; // 연장 일 수
}
