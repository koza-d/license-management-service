package koza.licensemanagementservice.dashboard.dto.response;

import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class LicenseStatsResponse {
    private long total;
    private Map<LicenseStatus, Long> byStatus;
    private BySession bySession;
    private long expiringWithin7d;

    @Getter
    @AllArgsConstructor
    public static class BySession {
        private long inUse;
        private long idle;
    }

    public static LicenseStatsResponse of(long activeCount, long expiredCount, long bannedCount,
                                          long inUse, long expiringWithin7d) {
        Map<LicenseStatus, Long> byStatus = new EnumMap<>(LicenseStatus.class);
        byStatus.put(LicenseStatus.ACTIVE, activeCount);
        byStatus.put(LicenseStatus.EXPIRED, expiredCount);
        byStatus.put(LicenseStatus.BANNED, bannedCount);

        long total = activeCount + expiredCount + bannedCount;
        long idle = Math.max(activeCount - inUse, 0);

        return new LicenseStatsResponse(total, byStatus, new BySession(inUse, idle), expiringWithin7d);
    }
}
