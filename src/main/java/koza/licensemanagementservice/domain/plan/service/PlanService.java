package koza.licensemanagementservice.domain.plan.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.plan.dto.PlanResponse;
import koza.licensemanagementservice.domain.plan.entity.PlanCode;
import koza.licensemanagementservice.domain.plan.repository.PlanRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<PlanResponse> getPlans() {
        return planRepository.findAll()
                .stream().map(PlanResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse getPlan(PlanCode planCode) {
        return PlanResponse.of(planRepository.findByPlanCode(planCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAN_NOT_FOUND)));
    }
}
