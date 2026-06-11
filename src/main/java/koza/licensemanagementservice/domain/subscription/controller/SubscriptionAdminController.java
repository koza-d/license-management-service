package koza.licensemanagementservice.domain.subscription.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.subscription.dto.AdminSubscriptionDetailResponse;
import koza.licensemanagementservice.domain.subscription.dto.AdminSubscriptionSummaryResponse;
import koza.licensemanagementservice.domain.subscription.dto.condition.SubscriptionAdminSearchCondition;
import koza.licensemanagementservice.domain.subscription.dto.request.AdminSubscriptionCancelRequest;
import koza.licensemanagementservice.domain.subscription.service.SubscriptionAdminService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/subscriptions")
@Tag(name = "[Admin] 구독 관련 API", description = "관리자 전용 구독 관련 API")
public class SubscriptionAdminController {
    private final SubscriptionAdminService subscriptionAdminService;

    @Operation(description = "구독 전체 조회 및 검색 API")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getSubscriptions(@AuthenticationPrincipal CustomUser user,
                                                           @ModelAttribute SubscriptionAdminSearchCondition condition,
                                                           Pageable pageable) {
        Page<AdminSubscriptionSummaryResponse> subscriptions = subscriptionAdminService.getSubscriptions(user, condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    @Operation(description = "구독 상세 조회 API")
    @GetMapping("/{subscriptionId}")
    public ResponseEntity<ApiResponse<?>> getSubscriptionDetail(@AuthenticationPrincipal CustomUser user,
                                                                @PathVariable Long subscriptionId) {
        AdminSubscriptionDetailResponse detail = subscriptionAdminService.getSubscriptionDetail(user, subscriptionId);
        ApiResponse<?> response = ApiResponse.success(detail);
        return ResponseEntity.ok(response);
    }

    @Operation(description = "구독 강제 취소 API")
    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelSubscription(@AuthenticationPrincipal CustomUser user,
                                                             @PathVariable Long subscriptionId,
                                                             @RequestBody @Valid AdminSubscriptionCancelRequest request) {
        subscriptionAdminService.cancel(user, subscriptionId, request);
        ApiResponse<?> response = ApiResponse.success("success!!");
        return ResponseEntity.ok(response);
    }

}
