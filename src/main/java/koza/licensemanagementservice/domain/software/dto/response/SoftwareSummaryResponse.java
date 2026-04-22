package koza.licensemanagementservice.domain.software.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SoftwareSummaryResponse {
    private Long id;
    private String name;
    private String version;
    private int licenseCount;
    private int activeSessionCount;
    private LocalDateTime createAt;

    @QueryProjection
    public SoftwareSummaryResponse(Long id, String name, String version, int licenseCount, int activeSessionCount, LocalDateTime createAt) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.licenseCount = licenseCount;
        this.activeSessionCount = activeSessionCount;
        this.createAt = createAt;
    }
}
