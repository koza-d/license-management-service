package koza.licensemanagementservice.domain.paymentmethod.service;

import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.paymentmethod.dto.PaymentMethodResponse;
import koza.licensemanagementservice.domain.paymentmethod.dto.RegisterPaymentMethodDTO;
import koza.licensemanagementservice.domain.paymentmethod.entity.PaymentMethod;
import koza.licensemanagementservice.domain.paymentmethod.repository.PaymentMethodRepository;
import koza.licensemanagementservice.global.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentMethodRegister {
    private final PaymentMethodRepository paymentMethodRepository;
    private final EncryptUtil encryptUtil;

    @Transactional
    public PaymentMethodResponse register(Member member, RegisterPaymentMethodDTO registerDTO) {
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .member(member)
                .billingKey(encryptUtil.encrypt(registerDTO.getBillingKey()))
                .cardType(registerDTO.getCardType())
                .cardOwnerType(registerDTO.getCardOwnerType())
                .cardCompany(registerDTO.getCardCompany())
                .cardNumberMasked(registerDTO.getCardNumberMasked())
                .authenticatedAt(registerDTO.getAuthenticatedAt())
                .active(true)
                .build();
        paymentMethodRepository.save(paymentMethod);
        return PaymentMethodResponse.of(paymentMethod);

    }
}
