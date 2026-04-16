package koza.licensemanagementservice.domain.software.repository;

import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SoftwareAdminSearchCondition {
    private SoftwareAdminSearchTarget target;
    private String search;
    private SoftwareStatus status;
    private LocalDate from;
    private LocalDate to;
}
