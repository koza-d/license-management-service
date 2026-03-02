package koza.licensemanagementservice.license.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import koza.licensemanagementservice.global.validation.JsonSize;
import koza.licensemanagementservice.license.entity.License;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class LicenseDTO {
    public static Long calcRemainingMs(LocalDateTime expiredAt) {
        // 0이면 만료, 양수면 기간 남음
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, expiredAt);
        return Math.max(0, duration.toMillis());
    }

    public static String maskLicenseKey(String licenseKey) {
        if (licenseKey == null || licenseKey.length() <= 4) return licenseKey;

        // 앞 4자리를 제외한 나머지를 *로 채우되, 하이픈 위치는 유지
        char[] chars = licenseKey.toCharArray();
        for (int i = 4; i < chars.length; i++) {
            if (chars[i] != '-') {
                chars[i] = '*';
            }
        }
        return new String(chars);
    }

    // 발급 시 요구 데이터
    @Getter
    public static class IssueRequest {
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

        @JsonSize(max = 5000, message = "메타 데이터 크기가 5000byte를 넘을 수 없습니다.")
        @Schema(description = "메타데이터", example = "{\"meta-data\": \"example\"}")
        private Map<String, Object> metadata;
    }


    // 연장 시 요구 데이터
    @Getter
    public static class ExtendRequest {
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

    @Getter
    @Builder
    // 연장 후 확인용 데이터
    public static class ExtendResponse {
        private String name;
        private String licenseKey;
        private String memo;
        private LocalDateTime expireAt; // 만료일자
        private long remainingMs; // 연장하고 남은 ms
        private int extendDays; // 연장 일 수
        private String status; // 라이센스 상태

        public static ExtendResponse of(License license, int extendDays) {
            long remainingMs = calcRemainingMs(license.getExpiredAt());

            return ExtendResponse.builder()
                    .name(license.getName())
                    .licenseKey(license.getLicenseKey())
                    .memo(license.getMemo())
                    .expireAt(license.getExpiredAt())
                    .remainingMs(remainingMs)
                    .extendDays(extendDays)
                    .status(license.getStatus().name())
                    .build();
        }
    }

    // 수정 시 요구 데이터 (모든 요소 변경함, 프론트에서 모든 값 전달 필요)
    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        @NotBlank(message = "라이센스 별칭은 필수 입력값입니다.")
        @Schema(description = "라이센스 별칭", example = "새이름입력")
        private String name;

        @Schema(description = "비고", example = "비고")
        private String memo;

        @JsonSize(max = 5000, message = "메타 데이터 크기가 5000byte를 넘을 수 없습니다.")
        @Schema(description = "메타데이터", example = "{\n" +
                "\n" +
                "  \"meta-data\": \"example\"\n" +
                "\n" +
                "}")
        private Map<String, Object> metadata;
    }

    // 발급 시 발급 확인용으로 출력될 데이터
    @Getter
    @Builder
    public static class IssueResponse {
        private String softwareName;
        private String licenseName;
        private String memo;
        private String licenseKey;
        private LocalDateTime expiredAt;
        private LocalDateTime createAt;

        public static IssueResponse from(License license) {
            return IssueResponse.builder()
                    .softwareName(license.getSoftware().getName())
                    .licenseName(license.getName())
                    .memo(license.getMemo())
                    .licenseKey(license.getLicenseKey())
                    .expiredAt(license.getExpiredAt())
                    .createAt(license.getCreateAt())
                    .build();
        }
    }

    // 상세관리에 보여줄 데이터 
    @Getter
    @Builder
    public static class DetailResponse {
        private String softwareName;
        private String softwareVersion;
        private String licenseName;
        private String memo;
        private String licenseKey;
        private LocalDateTime latestHeartbeatAt; // 마지막 하트비트 일시
        private LocalDateTime expiredAt;
        private long remainingMs; // 남은시간(ms)
        private String status;

        public static DetailResponse from(License license) {
            long remainingMs = calcRemainingMs(license.getExpiredAt());

            return DetailResponse.builder()
                    .softwareName(license.getSoftware().getName())
                    .softwareVersion(license.getSoftware().getVersion())
                    .licenseName(license.getName())
                    .memo(license.getMemo())
                    .licenseKey(license.getLicenseKey())
                    .latestHeartbeatAt(license.getLatestHearBeatAt())
                    .expiredAt(license.getExpiredAt())
                    .remainingMs(remainingMs)
                    .status(license.getStatus().name())
                    .build();
        }

    }

    // 목록에 보여줄 행 단위 데이터
    @Getter
    @Builder
    public static class SummaryResponse {
        private Long licenseId;
        private String name;
        private String licenseKey;
        private String memo; // 표로 보여줘야해서 10자 제한 출력
        private LocalDateTime expiredAt;
        private long remainingMs;
        private String status;

        public static SummaryResponse from(License license) {
            long remainingMs = calcRemainingMs(license.getExpiredAt());
            return SummaryResponse.builder()
                    .licenseId(license.getId())
                    .name(license.getName())
                    .licenseKey(maskLicenseKey(license.getLicenseKey())) // 첫 4자리 제외 마스킹(-제외)
                    .memo(license.getMemo())
                    .expiredAt(license.getExpiredAt())
                    .remainingMs(remainingMs)
                    .status(license.getStatus().name())
                    .build();
        }
    }
}
