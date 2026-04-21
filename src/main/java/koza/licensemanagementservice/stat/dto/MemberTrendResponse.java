package koza.licensemanagementservice.stat.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
public class MemberTrendResponse {
    private final LocalDate localDate;
    private final Long register;
    private final Long withdraw;
    private final Long increase;

    @QueryProjection
    public MemberTrendResponse(String dateFormat, Long register, Long withdraw) {
        this.localDate = LocalDate.parse(dateFormat);
        this.register = register;
        this.withdraw = withdraw;
        this.increase = register - withdraw;
    }
}
