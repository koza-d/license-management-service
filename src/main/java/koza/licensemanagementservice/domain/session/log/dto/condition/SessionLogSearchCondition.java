package koza.licensemanagementservice.domain.session.log.dto.condition;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SessionLogSearchCondition {
    private LocalDate from;
    private LocalDate to;
}
