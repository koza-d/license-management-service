package koza.licensemanagementservice.license.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import koza.licensemanagementservice.global.validation.JsonSize;
import lombok.Getter;

import java.util.Map;

@Getter
public class LicenseIssueRequest {
    @NotNull(message = "소프트웨어 정보가 없습니다.")
    @Schema(description = "소프트웨어 고유 ID", example = "1")
    private Long softwareId;

    @NotBlank(message = "라이센스 별칭은 필수 입력값입니다.")
    @Size(min = 1, max = 20, message = "별칭은 최소 1자 최대 20자입니다.")
    @Schema(description = "라이센스 별칭", example = "새이름입력")
    private String name;

    @Size(max = 20, message = "별칭은 최대 200자입니다.")
    @Schema(description = "비고", example = "비고")
    private String memo;

    @NotNull(message = "유효기간(일) 설정은 필수입니다.")
    @Schema(description = "유효기간(일)", example = "30")
    private int periodDays;

    @JsonSize(max = 5000, message = "지역변수 크기가 5000byte를 넘을 수 없습니다.")
    @Schema(description = "지역변수", example = "{\"meta-data\": \"example\"}")
    private Map<String, Object> localVariables;
}
