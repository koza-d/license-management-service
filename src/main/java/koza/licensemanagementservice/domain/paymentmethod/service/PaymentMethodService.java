package koza.licensemanagementservice.domain.paymentmethod.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.billing.toss.dto.TossBillingAuthRequest;
import koza.licensemanagementservice.domain.billing.toss.dto.TossBillingResponse;
import koza.licensemanagementservice.domain.billing.toss.service.TossBillingService;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.paymentmethod.dto.PaymentMethodResponse;
import koza.licensemanagementservice.domain.paymentmethod.dto.RegisterPaymentMethodDTO;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {
    private final PaymentMethodRegister paymentMethodRegister;
    private final TossBillingService tossBillingService;
    private final MemberRepository memberRepository;

    public PaymentMethodResponse issueTossBillingKey(CustomUser user, TossBillingAuthRequest request) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!member.getPaymentKey().equals(request.getCustomerKey()))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        TossBillingResponse response = tossBillingService.issueBillingKey(request);

        RegisterPaymentMethodDTO registerDTO = RegisterPaymentMethodDTO.of(response);
        return paymentMethodRegister.register(member, registerDTO);
    }

}
