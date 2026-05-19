package koza.licensemanagementservice.domain.plan.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.plan.dto.PlanResponse;
import koza.licensemanagementservice.domain.plan.service.PlanService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/plans")
@Tag(name = "플랜 API", description = "플랜 관련 API")
public class PlanController {
    private final PlanService planService;

    @Operation(description = "전체 플랜 조회 API")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getPlans() {
        List<PlanResponse> plans = planService.getPlans();
        ApiResponse<?> response = ApiResponse.success(plans);
        return ResponseEntity.ok(response);
    }
}
