package koza.licensemanagementservice.stat.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class VerificationAttemptTrend {
    private final LocalDate date;
    private final Long totalTry;
    private final Long success;
    private final Long fail;
    private final Double failRate;
    private Boolean isSpike = false; // 서비스단에서 계산

    @QueryProjection
    public VerificationAttemptTrend(String formattedDate, Long totalTry, Long success, Long fail) {
        this.date = LocalDate.parse(formattedDate);
        this.totalTry = totalTry;
        this.success = success;
        this.fail = fail;
        double rate = fail.doubleValue() / totalTry.doubleValue();
        this.failRate = totalTry.doubleValue() == 0 ? 0 : Math.round(rate * 10000) / 100.0;
    }

}
