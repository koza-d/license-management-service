package koza.licensemanagementservice.domain.member.log.repository;

import koza.licensemanagementservice.stat.dto.response.MemberTrendResponse;

import java.time.LocalDate;
import java.util.List;

public interface MemberLogRepositoryCustom {
    List<MemberTrendResponse> getMemberFlowTrend(LocalDate from, LocalDate to);
}
