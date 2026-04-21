package koza.licensemanagementservice.stat.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class LicenseStatusTrendResponse {
    private final LocalDate date;
    private final Long issued;
    private final Long expired;
    private final Long banned;

    @QueryProjection
    public LicenseStatusTrendResponse(String formattedDate, Long issued, Long expired, Long banned) {
        this.date = LocalDate.parse(formattedDate);
        this.issued = issued;
        this.expired = expired;
        this.banned = banned;
    }
}

