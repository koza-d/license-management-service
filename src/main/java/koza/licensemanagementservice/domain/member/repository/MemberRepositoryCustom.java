package koza.licensemanagementservice.domain.member.repository;

import koza.licensemanagementservice.stat.dto.MemberPlanDistributionResponse;

public interface MemberRepositoryCustom {
    MemberPlanDistributionResponse getMemberPlanDistribution();
}
