package koza.licensemanagementservice.domain.paymentmethod.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.billing.toss.dto.TossBillingAuthRequest;
import koza.licensemanagementservice.domain.billing.toss.dto.TossBillingResponse;
import koza.licensemanagementservice.domain.billing.toss.service.TossBillingService;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.paymentmethod.dto.PaymentMethodResponse;
import koza.licensemanagementservice.domain.paymentmethod.dto.RegisterPaymentMethodDTO;
import koza.licensemanagementservice.domain.paymentmethod.entity.PaymentMethod;
import koza.licensemanagementservice.domain.paymentmethod.repository.PaymentMethodRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {
    private final PaymentMethodRegister paymentMethodRegister;
    private final TossBillingService tossBillingService;
    private final MemberRepository memberRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    @Transactional(readOnly = true)
    public PaymentMethodResponse issueTossBillingKey(CustomUser user, TossBillingAuthRequest request) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!member.getPaymentKey().equals(request.getCustomerKey()))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        TossBillingResponse response = tossBillingService.issueBillingKey(request);

        Optional<PaymentMethod> existPaymentMethod = paymentMethodRepository.findByMemberIdAndCardCompanyAndCardNumberMaskedAndIsActiveIsTrue(member.getId(), response.getCardCompany(), response.getCardNumber());
        if (existPaymentMethod.isPresent())
            throw new BusinessException(ErrorCode.PAYMENT_METHOD_DUPLICATE);

        RegisterPaymentMethodDTO registerDTO = RegisterPaymentMethodDTO.of(response);
        return paymentMethodRegister.register(member, registerDTO);
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getPaymentMethods(CustomUser user) {
        return paymentMethodRepository.findByMemberId(user.getId())
                .stream().map(PaymentMethodResponse::of)
                .toList();
    }


    @Transactional
    public void updateDefault(CustomUser user, Long paymentMethodId) {
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findByMemberId(user.getId());
        for (PaymentMethod method : paymentMethods) {
            if (method.getId().equals(paymentMethodId))
                method.setDefault(true);
            else if (method.isDefault())
                method.setDefault(false);
        }
    }

    @Transactional
    public void delete(CustomUser user, Long paymentMethodId) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        if (!paymentMethod.getMember().getId().equals(user.getId())) // 로그인 유저의 결제수단이 아닌경우
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        if (paymentMethod.isDefault()) // 기본값으로 설정된 결제수단인 경우
            throw new BusinessException(ErrorCode.PAYMENT_METHOD_CANNOT_DELETE_DEFAULT);

        paymentMethod.delete();
    }
}
