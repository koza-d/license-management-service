package koza.licensemanagementservice.session.dto;

import koza.licensemanagementservice.session.repository.SessionLogRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyUsageResponse {
    private LocalDate date;
    private long minutes;
    public static DailyUsageResponse from(SessionLogRepository.DailyUsageResponse r) {
        return new DailyUsageResponse(
                r.getDate(),
                r.getMinutes()
        );
    }
}
