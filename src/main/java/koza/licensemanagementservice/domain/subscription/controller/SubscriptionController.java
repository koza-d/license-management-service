package koza.licensemanagementservice.domain.subscription.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.subscription.dto.SubscriptionResponse;
import koza.licensemanagementservice.domain.subscription.dto.SubscriptionStartRequest;
import koza.licensemanagementservice.domain.subscription.service.SubscriptionService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
@Tag(name = "구독 API", description = "구독 관련 API")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @Operation(summary = "구독 정보 조회")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<?>> getSubscription(@AuthenticationPrincipal CustomUser user) {
        SubscriptionResponse subscription = subscriptionService.getSubscription(user);
        ApiResponse<?> response = ApiResponse.success(subscription);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "구독 시작")
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<?>> start(@AuthenticationPrincipal CustomUser user,
                                                @RequestBody @Valid SubscriptionStartRequest request) {
        subscriptionService.start(user, request);
        ApiResponse<?> response = ApiResponse.success("success!!");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "구독 갱신")
    @PostMapping("/{subscriptionId}/renewal")
    public ResponseEntity<ApiResponse<?>> renewal(@AuthenticationPrincipal CustomUser user,
                                                  @PathVariable("subscriptionId") Long subscriptionId) {
        subscriptionService.renewal(user, subscriptionId);
        ApiResponse<?> response = ApiResponse.success("success!!");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "구독 취소")
    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<ApiResponse<?>> cancel(@AuthenticationPrincipal CustomUser user,
                                                 @PathVariable("subscriptionId") Long subscriptionId) {
        subscriptionService.cancel(user, subscriptionId);
        ApiResponse<?> response = ApiResponse.success("success!!");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "구독 재개")
    @PostMapping("/{subscriptionId}/resume")
    public ResponseEntity<ApiResponse<?>> resume(@AuthenticationPrincipal CustomUser user,
                                                 @PathVariable("subscriptionId") Long subscriptionId) {
        subscriptionService.resume(user, subscriptionId);
        ApiResponse<?> response = ApiResponse.success("success!!");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "해당 구독에 대한 결제수단 변경")
    @PutMapping("/{subscriptionId}/payment-method")
    public ResponseEntity<ApiResponse<?>> changePaymentMethod(@AuthenticationPrincipal CustomUser user,
                                                              @PathVariable("subscriptionId") Long subscriptionId,
                                                              @RequestBody Map<String, Long> requestData) {
        Long paymentMethodId = requestData.get("paymentMethodId");
        subscriptionService.changePaymentMethod(user, subscriptionId, paymentMethodId);
        ApiResponse<?> response = ApiResponse.success("success!!");
        return ResponseEntity.ok(response);
    }

}
