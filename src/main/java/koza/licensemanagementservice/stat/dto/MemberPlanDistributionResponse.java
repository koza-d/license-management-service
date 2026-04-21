package koza.licensemanagementservice.stat.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class MemberPlanDistributionResponse {
    private final Long basic;
    private final Long premium;
    private final Long enterprise;

    @QueryProjection
    public MemberPlanDistributionResponse(Long basic, Long premium, Long enterprise) {
        this.basic = basic;
        this.premium = premium;
        this.enterprise = enterprise;
    }
}
