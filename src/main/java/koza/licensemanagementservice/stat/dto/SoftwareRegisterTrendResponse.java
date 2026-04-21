package koza.licensemanagementservice.stat.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SoftwareRegisterTrendResponse {
    private final LocalDate date;
    private final Long register;

    @QueryProjection
    public SoftwareRegisterTrendResponse(String formattedDate, Long register) {
        this.date = LocalDate.parse(formattedDate);
        this.register = register;
    }
}
