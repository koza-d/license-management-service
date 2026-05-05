package koza.licensemanagementservice.domain.license.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import lombok.Getter;

@Getter
public class LicenseStatusCount {
    private final LicenseStatus status;
    private final long count;

    @QueryProjection
    public LicenseStatusCount(LicenseStatus status, Long count) {
        this.status = status;
        this.count = count != null ? count : 0L;
    }
}
