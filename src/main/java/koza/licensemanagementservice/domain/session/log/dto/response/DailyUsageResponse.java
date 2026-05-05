package koza.licensemanagementservice.domain.session.log.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyUsageResponse {
    private LocalDate date;
    private long minutes;

    // Querydsl 에서 MYSQL 함수 Date() 쿼리 결과가 LocalDate 로 변환 안돼서 java.sql.Date로 받음
    @QueryProjection
    public DailyUsageResponse(java.sql.Date date, long minutes) {
        this.date = date.toLocalDate();
        this.minutes = minutes;
    }
}
