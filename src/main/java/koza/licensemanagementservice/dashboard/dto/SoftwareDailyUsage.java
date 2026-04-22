package koza.licensemanagementservice.dashboard.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class SoftwareDailyUsage {
    private Long softwareId;
    private LocalDate date;
    private Long minutes;

    // Querydsl 에서 MYSQL 함수 Date() 쿼리 결과가 DateTime 로 변환 안돼서 java.sql.Date로 받음
    @QueryProjection
    public SoftwareDailyUsage(Long softwareId, java.sql.Date date, Long minutes) {
        this.softwareId = softwareId;
        this.date = date.toLocalDate();
        this.minutes = minutes;
    }
}
