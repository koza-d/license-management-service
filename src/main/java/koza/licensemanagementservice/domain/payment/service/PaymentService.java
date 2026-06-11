package koza.licensemanagementservice.domain.payment.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.payment.dto.PaymentDetailResponse;
import koza.licensemanagementservice.domain.payment.dto.PaymentSummaryResponse;
import koza.licensemanagementservice.domain.payment.entity.Payment;
import koza.licensemanagementservice.domain.payment.repository.PaymentRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Page<PaymentSummaryResponse> getPayments(CustomUser user, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByMemberId(user.getId(), pageable);
        return payments.map(PaymentSummaryResponse::of);
    }

    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentDetail(CustomUser user, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!payment.getMember().getId().equals(user.getId()))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        return PaymentDetailResponse.of(payment);
    }
}
