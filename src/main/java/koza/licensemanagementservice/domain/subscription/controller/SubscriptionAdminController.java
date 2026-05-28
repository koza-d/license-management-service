package koza.licensemanagementservice.domain.subscription.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.subscription.dto.AdminSubscriptionSummaryResponse;
import koza.licensemanagementservice.domain.subscription.dto.condition.SubscriptionAdminSearchCondition;
import koza.licensemanagementservice.domain.subscription.service.SubscriptionAdminService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
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
        System.out.println("condition = " + condition);
        System.out.println("pageable = " + pageable);
        Page<AdminSubscriptionSummaryResponse> subscriptions = subscriptionAdminService.getSubscriptions(user, condition, pageable);
        ApiResponse<?> response = ApiResponse.success(subscriptions);
        return ResponseEntity.ok(response);
    }

}
