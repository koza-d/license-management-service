package koza.licensemanagementservice.domain.paymentmethod.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.billing.toss.dto.TossBillingAuthRequest;
import koza.licensemanagementservice.domain.paymentmethod.dto.PaymentMethodResponse;
import koza.licensemanagementservice.domain.paymentmethod.service.PaymentMethodService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/payment-method")
@Tag(name = "결제수단 API", description = "결제수단 관련 API")
public class PaymentMethodController {
    private final PaymentMethodService paymentMethodService;

    @Operation(description = "토스 빌링키 발급 API")
    @PostMapping("/toss/issue")
    public ResponseEntity<ApiResponse<?>> issueTossBillingKey(@AuthenticationPrincipal CustomUser user,
                                                              TossBillingAuthRequest request) {
        PaymentMethodResponse paymentMethodResponse = paymentMethodService.issueTossBillingKey(user, request);
        ApiResponse<?> response = ApiResponse.success(paymentMethodResponse);
        return ResponseEntity.ok(response);
    }

}
