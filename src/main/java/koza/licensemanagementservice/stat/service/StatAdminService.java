package koza.licensemanagementservice.stat.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.license.log.repository.LicenseLogRepository;
import koza.licensemanagementservice.domain.member.log.repository.MemberLogRepository;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.session.log.repository.SessionLogRepository;
import koza.licensemanagementservice.domain.session.log.repository.SessionLogRepository.SessionPeakInterface;
import koza.licensemanagementservice.domain.software.log.repository.SoftwareLogRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.global.util.FillGaps;
import koza.licensemanagementservice.stat.dto.*;
import koza.licensemanagementservice.verification.log.repository.VerifyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static koza.licensemanagementservice.global.validation.ValidUserAuthorized.*;

@Service
@RequiredArgsConstructor
public class StatAdminService {
    private final MemberRepository memberRepository;
    private final MemberLogRepository memberLogRepository;
    private final SoftwareLogRepository softwareLogRepository;
    private final SessionLogRepository sessionLogRepository;
    private final LicenseLogRepository licenseLogRepository;
    private final VerifyLogRepository verifyLogRepository;

    public List<MemberTrendResponse> getMemberTrend(CustomUser user, LocalDate from, LocalDate to) {
        validAdminAuthorized(user);

        if (from != null && to != null && from.isAfter(to))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        List<MemberTrendResponse> result = memberLogRepository.getMemberFlowTrend(from, to);

        return FillGaps.fillDateGaps(from, to, result,
                MemberTrendResponse::getLocalDate,
                date -> new MemberTrendResponse(date.toString(), 0L, 0L));
    }

    public MemberPlanDistributionResponse getMemberPlanDistribution(CustomUser user) {
        validAdminAuthorized(user);

        return memberRepository.getMemberPlanDistribution();
    }

    public List<SoftwareRegisterTrendResponse> getSoftwareRegistrationTrends(CustomUser user, LocalDate from, LocalDate to) {
        validAdminAuthorized(user);

        List<SoftwareRegisterTrendResponse> result = softwareLogRepository.getSoftwareRegistrationTrends(from, to);

        return FillGaps.fillDateGaps(from, to, result,
                SoftwareRegisterTrendResponse::getDate,
                date -> new SoftwareRegisterTrendResponse(date.toString(), 0L));
    }

    public List<SoftwareUsageResponse> getSoftwareTopNUsage(CustomUser user, Integer topN) {
        validAdminAuthorized(user);

        return sessionLogRepository.getTopNSoftwareByUsageTime(topN);
    }

    public List<LicenseStatusTrendResponse> getLicenseStatusTrends(CustomUser user, LocalDate from, LocalDate to) {
        validAdminAuthorized(user);

        List<LicenseStatusTrendResponse> result = licenseLogRepository.getLicenseStatusTrendsByDate(from, to);

        return FillGaps.fillDateGaps(from, to, result,
                LicenseStatusTrendResponse::getDate,
                date -> new LicenseStatusTrendResponse(date.toString(), 0L, 0L, 0L));
    }

    public List<VerificationAttemptTrend> getVerificationMetrics(CustomUser user, LocalDate from, LocalDate to) {
        validAdminAuthorized(user);

        List<VerificationAttemptTrend> result = verifyLogRepository.getVerificationMetrics(from, to);

        List<VerificationAttemptTrend> trends = FillGaps.fillDateGaps(from, to, result,
                VerificationAttemptTrend::getDate,
                date -> new VerificationAttemptTrend(date.toString(), 0L, 0L, 0L));

        setSpikes(trends);
        return trends;
    }

    public void setSpikes(List<VerificationAttemptTrend> trends) {
        if (trends.isEmpty()) return;

        double[] failRates = trends.stream()
                .mapToDouble(VerificationAttemptTrend::getFailRate)
                .toArray();

        double mean = Arrays.stream(failRates).average().orElse(0.0);

        double variance = Arrays.stream(failRates)
                .map(x -> Math.pow(x - mean, 2))
                .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);

        double threshold = mean + (2.0 * stdDev);

        for (VerificationAttemptTrend trend : trends) {
            if (trend.getFailRate() > threshold)
                trend.setIsSpike(true);
        }
    }

    public List<SessionPeakResponse> getSessionPeakByDays(CustomUser user, LocalDate from, LocalDate to) {
        validAdminAuthorized(user);
        List<SessionPeakInterface> result = sessionLogRepository.findPeakByDays(from.atStartOfDay(), to.atTime(LocalTime.MAX));
        Map<Integer, SessionPeakInterface> resultMap = result.stream()
                .collect(Collectors.toMap(SessionPeakInterface::getUnit, i -> i));

        return fillGapsBySessionPeak(7, resultMap);
    }

    public List<SessionPeakResponse> getSessionPeakByHours(CustomUser user, LocalDate from, LocalDate to) {
        validAdminAuthorized(user);

        List<SessionPeakInterface> result = sessionLogRepository.findPeakByHours(from.atStartOfDay(), to.atTime(LocalTime.MAX));
        Map<Integer, SessionPeakInterface> resultMap = result.stream()
                .collect(Collectors.toMap(SessionPeakInterface::getUnit, i -> i));

        return fillGapsBySessionPeak(24, resultMap);
    }

    private static List<SessionPeakResponse> fillGapsBySessionPeak(int endExclusive, Map<Integer, SessionPeakInterface> resultMap) {
        return IntStream.range(0, endExclusive)
                .mapToObj(unit -> {
                    if (resultMap.containsKey(unit)) {
                        SessionPeakInterface dbData = resultMap.get(unit);
                        return new SessionPeakResponse(dbData.getUnit(), dbData.getAvg(), dbData.getMax());
                    } else {
                        return new SessionPeakResponse(unit, 0.0, 0);
                    }
                })
                .collect(Collectors.toList());
    }
}
