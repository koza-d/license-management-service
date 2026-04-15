package koza.licensemanagementservice.domain.session.log.repository;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SessionLogSearchCondition {
    private LocalDate from;
    private LocalDate to;
}
