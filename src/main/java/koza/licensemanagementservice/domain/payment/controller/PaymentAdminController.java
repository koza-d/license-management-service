package koza.licensemanagementservice.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.payment.dto.condition.PaymentAdminSearchCondition;
import koza.licensemanagementservice.domain.payment.dto.request.AdminPaymentSuccessRequest;
import koza.licensemanagementservice.domain.payment.dto.request.AdminPaymentFailRequest;
import koza.licensemanagementservice.domain.payment.dto.request.AdminPaymentRefundRequest;
import koza.licensemanagementservice.domain.payment.dto.response.AdminPaymentDetailResponse;
import koza.licensemanagementservice.domain.payment.dto.response.AdminPaymentSummaryResponse;
import koza.licensemanagementservice.domain.payment.service.PaymentAdminService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/payments")
@Tag(name = "[Admin] 결제 관련 API", description = "관리자 전용 결제 관련 API")
public class PaymentAdminController {
    private final PaymentAdminService paymentAdminService;

    @Operation(description = "결제 내역 목록 조회 및 검색 API")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getPayments(@AuthenticationPrincipal CustomUser user,
                                                      @ModelAttribute PaymentAdminSearchCondition condition,
                                                      Pageable pageable) {
        Page<AdminPaymentSummaryResponse> payments = paymentAdminService.getPayments(user, condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @Operation(description = "결제 내역 상세 조회 API")
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<?>> getPaymentDetail(@AuthenticationPrincipal CustomUser user,
                                                           @PathVariable Long paymentId) {
        AdminPaymentDetailResponse detail = paymentAdminService.getPaymentDetail(user, paymentId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @Operation(description = "구독별 결제 내역 목록 조회 API")
    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<ApiResponse<?>> getPaymentsBySubscription(@AuthenticationPrincipal CustomUser user,
                                                                    @PathVariable Long subscriptionId) {
        List<AdminPaymentDetailResponse> payments = paymentAdminService.getPaymentsBySubscription(user, subscriptionId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @Operation(description = "PENDING 결제 자동 해소 API")
    @PostMapping("/{paymentId}/resolve")
    public ResponseEntity<ApiResponse<?>> resolvePayment(@AuthenticationPrincipal CustomUser user,
                                                         @PathVariable Long paymentId) {
        paymentAdminService.resolve(user, paymentId);
        return ResponseEntity.ok(ApiResponse.success("success!!"));
    }

    @Operation(description = "PENDING 결제 수동 승인 처리 API")
    @PostMapping("/{paymentId}/success")
    public ResponseEntity<ApiResponse<?>> approvePayment(@AuthenticationPrincipal CustomUser user,
                                                      @PathVariable Long paymentId,
                                                      @RequestBody @Valid AdminPaymentSuccessRequest request) {
        paymentAdminService.manualSuccess(user, paymentId, request);
        return ResponseEntity.ok(ApiResponse.success("success!!"));
    }

    @Operation(description = "PENDING 결제 수동 실패 처리 API")
    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<ApiResponse<?>> failPayment(@AuthenticationPrincipal CustomUser user,
                                                      @PathVariable Long paymentId,
                                                      @RequestBody @Valid AdminPaymentFailRequest request) {
        paymentAdminService.fail(user, paymentId, request);
        return ResponseEntity.ok(ApiResponse.success("success!!"));
    }

    @Operation(description = "결제 환불 API")
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<ApiResponse<?>> refundPayment(@AuthenticationPrincipal CustomUser user,
                                                        @PathVariable Long paymentId,
                                                        @RequestBody @Valid AdminPaymentRefundRequest request) {
        paymentAdminService.refund(user, paymentId, request);
        return ResponseEntity.ok(ApiResponse.success("success!!"));
    }
}
