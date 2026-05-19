package koza.licensemanagementservice.domain.plan.service;

import koza.licensemanagementservice.domain.plan.dto.PlanResponse;
import koza.licensemanagementservice.domain.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;

    public List<PlanResponse> getPlans() {
        return planRepository.findAll()
                .stream().map(PlanResponse::of)
                .toList();
    }
}
