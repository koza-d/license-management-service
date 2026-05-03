package koza.licensemanagementservice.dashboard.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class ExpiringLicenseResponse {
    private final Long licenseId;
    private final String licenseName;
    private final String licenseKey;
    private final String ownerNickname;
    private final String softwareName;
    private final LocalDateTime expiredAt;
    private final long remainingMs;

    @QueryProjection
    public ExpiringLicenseResponse(Long licenseId, String licenseName, String licenseKey,
                                   String ownerNickname, String softwareName, LocalDateTime expiredAt) {
        this.licenseId = licenseId;
        this.licenseName = licenseName;
        this.licenseKey = licenseKey;
        this.ownerNickname = ownerNickname;
        this.softwareName = softwareName;
        this.expiredAt = expiredAt;
        long diff = Duration.between(LocalDateTime.now(), expiredAt).toMillis();
        this.remainingMs = Math.max(diff, 0L);
    }
}
