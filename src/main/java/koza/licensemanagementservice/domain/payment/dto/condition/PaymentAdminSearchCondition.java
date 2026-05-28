package koza.licensemanagementservice.domain.payment.dto.condition;

import koza.licensemanagementservice.domain.payment.entity.PaymentStatus;

import java.time.LocalDate;
import java.util.List;

public record PaymentAdminSearchCondition(
        PaymentAdminSearchTarget target,
        String search,
        List<PaymentStatus> statusFilters,
        LocalDate from,
        LocalDate to
) {
}
