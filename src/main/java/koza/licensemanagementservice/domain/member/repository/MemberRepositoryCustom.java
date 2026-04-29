package koza.licensemanagementservice.domain.member.repository;

import koza.licensemanagementservice.stat.dto.response.MemberPlanDistributionResponse;

public interface MemberRepositoryCustom {
    MemberPlanDistributionResponse getMemberPlanDistribution();
}
