package koza.licensemanagementservice.domain.plan.dto;

import koza.licensemanagementservice.domain.plan.entity.Plan;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanResponse {
    private String name;
    private String planCode;
    private int monthlyPrice;
    private int yearlyPrice;
    private int limitLicense;
    private int limitSoftware;

    public static PlanResponse of(Plan plan) {
        return PlanResponse.builder()
                .name(plan.getName())
                .planCode(plan.getPlanCode())
                .monthlyPrice(plan.getMonthlyPrice())
                .yearlyPrice(plan.getYearlyPrice())
                .limitLicense(plan.getLimitLicense())
                .limitSoftware(plan.getLimitSoftware())
                .build();
    }
}
