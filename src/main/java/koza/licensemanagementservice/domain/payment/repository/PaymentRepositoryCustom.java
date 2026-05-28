package koza.licensemanagementservice.domain.payment.repository;

import koza.licensemanagementservice.domain.payment.dto.condition.PaymentAdminSearchCondition;
import koza.licensemanagementservice.domain.payment.dto.response.AdminPaymentSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentRepositoryCustom {
    Page<AdminPaymentSummaryResponse> searchPayments(PaymentAdminSearchCondition condition, Pageable pageable);
}
