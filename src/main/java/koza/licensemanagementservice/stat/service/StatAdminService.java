package koza.licensemanagementservice.stat.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.license.log.repository.LicenseLogRepository;
import koza.licensemanagementservice.domain.member.log.repository.MemberLogRepository;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.session.log.repository.SessionLogRepository;
import koza.licensemanagementservice.domain.software.log.repository.SoftwareLogRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.stat.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static koza.licensemanagementservice.global.validation.ValidUserAuthorized.*;

@Service
@RequiredArgsConstructor
public class StatAdminService {
    private final MemberRepository memberRepository;
    private final MemberLogRepository memberLogRepository;
    private final SoftwareLogRepository softwareLogRepository;
    private final SessionLogRepository sessionLogRepository;
    private final LicenseLogRepository licenseLogRepository;

    public List<MemberTrendResponse> getMemberTrend(CustomUser user, LocalDate from, LocalDate to) {
        validAdminAuthorized(user);

        if (from != null && to != null && from.isAfter(to))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        List<MemberTrendResponse> memberFlowTrend = new ArrayList<>();
        List<MemberTrendResponse> result = memberLogRepository.getMemberFlowTrend(from, to);

        // 날짜 : 당일 변화량
        Map<LocalDate, MemberTrendResponse> resultMap = result.stream()
                .collect(Collectors.toMap(MemberTrendResponse::getLocalDate, r -> r));

        // 시작일부터 종료일까지 루프
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            // 해당일 데이터가 없으면 0,0으로 채움
            MemberTrendResponse response = resultMap.getOrDefault(
                    date,
                    new MemberTrendResponse(date.toString(), 0L, 0L)
            );
            memberFlowTrend.add(response);
        }

        return memberFlowTrend;
    }

    public MemberPlanDistributionResponse getMemberPlanDistribution(CustomUser user) {
        validAdminAuthorized(user);

        return memberRepository.getMemberPlanDistribution();
    }

    public List<SoftwareRegisterTrendResponse> getSoftwareRegistrationTrends(CustomUser user, LocalDate from, LocalDate to) {
        validAdminAuthorized(user);

        List<SoftwareRegisterTrendResponse> softwareTrends = new ArrayList<>();
        List<SoftwareRegisterTrendResponse> result = softwareLogRepository.getSoftwareRegistrationTrends(from, to);
        Map<LocalDate, SoftwareRegisterTrendResponse> resultMap = result.stream()
                .collect(Collectors.toMap(SoftwareRegisterTrendResponse::getDate, r -> r));

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            SoftwareRegisterTrendResponse response = resultMap.getOrDefault(date,
                    new SoftwareRegisterTrendResponse(date.toString(), 0L));

            softwareTrends.add(response);
        }

        return softwareTrends;
    }

    public List<SoftwareUsageResponse> getSoftwareTopNUsage(CustomUser user, Integer topN) {
        validAdminAuthorized(user);

        return sessionLogRepository.getTopNSoftwareByUsageTime(topN);
    }

    public List<LicenseStatusTrendResponse> getLicenseStatusTrends(CustomUser user, LocalDate from, LocalDate to) {
        validAdminAuthorized(user);

        List<LicenseStatusTrendResponse> licenseStatusTrends = new ArrayList<>();
        List<LicenseStatusTrendResponse> result = licenseLogRepository.getLicenseStatusTrendsByDate(from, to);
        Map<LocalDate, LicenseStatusTrendResponse> resultMap = result.stream()
                .collect(Collectors.toMap(LicenseStatusTrendResponse::getDate, r -> r));

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            LicenseStatusTrendResponse response = resultMap.getOrDefault(date,
                    new LicenseStatusTrendResponse(date.toString(), 0L, 0L, 0L));

            licenseStatusTrends.add(response);
        }
        return licenseStatusTrends;
    }
}
