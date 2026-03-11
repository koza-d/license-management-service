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
