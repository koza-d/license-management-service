package koza.licensemanagementservice.software.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import koza.licensemanagementservice.global.validation.JsonSize;
import koza.licensemanagementservice.software.entity.Software;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

public class SoftwareDTO {

    // 등록 시 요구 데이터
    @Getter
    public static class CreateRequest {
        @NotBlank(message = "소프트웨어 이름은 필수 입력 값입니다.")
        @Size(min = 5, max = 30, message = "소프트웨어 이름은 최소 5글자 최대 30글자 입니다.")
        @Schema(description = "소프트웨어 이름", example = "테스트 소프트웨어")
        private String name;

        @NotBlank(message = "버전은 필수입니다.")
        @Pattern(
                regexp = "^[0-9]+\\.[0-9]+\\.[0-9]+$",
                message = "버전 형식은 1.0.0과 같아야 합니다."
        )
        @Schema(description = "소프트웨어 최신 버전(ex. 1.0.0)", example = "1.0.0")
        private String latestVersion;

        @JsonSize(max = 5000, message = "전역변수의 크기는 최대 5000byte를 넘을 수 없습니다.")
        @Schema(description = "소프트웨어 전역변수, 소속 라이센스마다 동일한 값을 가짐", example = "{}")
        private Map<String, Object> globalVariables;

        @JsonSize(max = 5000, message = "지역변수의 크기는 최대 5000byte를 넘을 수 없습니다.")
        @Schema(description = "소프트웨어 지역변수, value는 기본값", example = "{}")
        private Map<String, Object> localVariables;
    }

    // 등록 후 반환 데이터
    @Getter
    @Builder
    public static class CreateResponse {
        private Long id;
        private String name;
        private String latestVersion;
        private String apiKey;
        private int limitLicense;
        private Map<String, Object> globalVariables;
        private Map<String, Object> localVariables;
        public static CreateResponse from(Software software) {
            return CreateResponse.builder()
                    .id(software.getId())
                    .name(software.getName())
                    .latestVersion(software.getLatestVersion())
                    .apiKey(software.getApiKey())
                    .limitLicense(software.getLimitLicense())
                    .globalVariables(software.getGlobalVariables())
                    .localVariables(software.getLocalVariables())
                    .build();
        }
    }

    // 변경 시 요구 데이터 (요소 전부 다 변경, 프론트에서 모든 값 전달 필요)
    @Getter
    public static class UpdateRequest {
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


    // 상세조회
    @Getter
    @Builder
    public static class DetailResponse {
        private Long id;
        private String name;
        private String latestVersion;
        private String apiKey;
        private int licenseCount;
        private int limitLicense;
        private int remainLicense;
        private Map<String, Object> globalVariables;
        private Map<String, Object> localVariables;
        private LocalDateTime createAt;

        public static DetailResponse of(Software software, int licenseCount) {
            return DetailResponse.builder()
                    .id(software.getId())
                    .name(software.getName())
                    .latestVersion(software.getLatestVersion())
                    .apiKey(software.getApiKey())
                    .licenseCount(licenseCount)
                    .limitLicense(software.getLimitLicense())
                    .remainLicense(software.getLimitLicense() - licenseCount)
                    .globalVariables(software.getGlobalVariables())
                    .localVariables(software.getLocalVariables())
                    .createAt(software.getCreateAt())
                    .build();
        }
    }

    // 목록 조회
    @Getter
    @Builder
    public static class SummaryResponse {
        private Long id;
        private String name;
        private String version;
        private int licenseCount;
        private int activeSessionCount;
        private LocalDateTime createAt;

        @QueryProjection
        public SummaryResponse(Long id, String name, String version, int licenseCount, int activeSessionCount, LocalDateTime createAt) {
            this.id = id;
            this.name = name;
            this.version = version;
            this.licenseCount = licenseCount;
            this.activeSessionCount = activeSessionCount;
            this.createAt = createAt;
        }

    }
}
