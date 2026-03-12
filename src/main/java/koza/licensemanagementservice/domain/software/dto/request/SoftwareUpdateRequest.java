package koza.licensemanagementservice.domain.software.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import koza.licensemanagementservice.global.validation.JsonSize;
import lombok.Getter;

import java.util.Map;

@Getter
public class SoftwareUpdateRequest {
    @NotBlank(message = "소프트웨어 이름은 필수 입력 값입니다.")
    @Size(min = 5, max = 30, message = "소프트웨어 이름은 최소 5글자 최대 30글자 입니다.")
    @Schema(description = "소프트웨어 이름", example = "테스트 소프트웨어")
    private String name;

    @NotBlank(message = "버전은 필수입니다.")
    @Pattern(
            regexp = "^[0-9]+\\.[0-9]+\\.[0-9]+$",
            message = "버전 형식은 1.0.0과 같아야 합니다."
    )
    @Schema(description = "소프트웨어 버전(ex. 1.0.0)", example = "1.0.0")
    private String version;

    @JsonSize(max = 5000, message = "전역변수의 크기는 최대 5000byte를 넘을 수 없습니다.")
    @Schema(description = "소프트웨어 전역변수, 소속 라이센스마다 동일한 값을 가짐", example = "{}")
    private Map<String, Object> globalVariables;

    @JsonSize(max = 5000, message = "지역변수의 크기는 최대 5000byte를 넘을 수 없습니다.")
    @Schema(description = "소프트웨어 지역변수, value는 기본값", example = "{}")
    private Map<String, Object> localVariables;
}
