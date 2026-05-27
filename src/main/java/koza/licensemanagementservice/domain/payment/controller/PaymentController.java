package koza.licensemanagementservice.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.payment.dto.PaymentDetailResponse;
import koza.licensemanagementservice.domain.payment.dto.PaymentSummaryResponse;
import koza.licensemanagementservice.domain.payment.service.PaymentService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "결제내역 API", description = "결제내역 관련 API")
public class PaymentController {
    private final PaymentService paymentService;

    @Operation(summary = "결제내역 페이징 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getPayments(@AuthenticationPrincipal CustomUser user,
                                                      @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PaymentSummaryResponse> paymentsResponse = paymentService.getPayments(user, pageable);
        ApiResponse<?> response = ApiResponse.success(paymentsResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "결제내역 상세 조회")
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<?>> getPaymentDetail(@AuthenticationPrincipal CustomUser user,
                                                           @PathVariable("paymentId") Long paymentId) {
        PaymentDetailResponse paymentDetail = paymentService.getPaymentDetail(user, paymentId);
        ApiResponse<?> response = ApiResponse.success(paymentDetail);
        return ResponseEntity.ok(response);
    }
}
