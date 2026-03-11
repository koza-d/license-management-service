package koza.licensemanagementservice.license.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import koza.licensemanagementservice.global.validation.JsonSize;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class LicenseUpdateRequest {
    @NotBlank(message = "라이센스 별칭은 필수 입력값입니다.")
    @Schema(description = "라이센스 별칭", example = "새이름입력")
    private String name;

    @Schema(description = "비고", example = "비고")
    private String memo;

    @JsonSize(max = 5000, message = "지역변수 크기가 5000byte를 넘을 수 없습니다.")
    @Schema(description = "지역변수", example = "{\n" +
            "\n" +
            "  \"meta-data\": \"example\"\n" +
            "\n" +
            "}")
    private Map<String, Object> localVariables;
}
