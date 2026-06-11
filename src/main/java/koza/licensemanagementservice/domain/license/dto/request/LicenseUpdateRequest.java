package koza.licensemanagementservice.domain.license.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @Size(max = 200, message = "비고는 최대 200자입니다.")
    @Schema(description = "비고", example = "비고")
    private String memo;

    @JsonSize
    @Schema(description = "지역변수", example = "{\n" +
            "\n" +
            "  \"meta-data\": \"example\"\n" +
            "\n" +
            "}")
    private Map<String, String> localVariables;
}
