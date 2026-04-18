package koza.licensemanagementservice.stat.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.member.log.repository.MemberLogRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.global.validation.ValidUserAuthorized;
import koza.licensemanagementservice.stat.dto.MemberTrendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatAdminService {
    private final MemberLogRepository memberLogRepository;

    public List<MemberTrendResponse> getMemberTrend(CustomUser user, LocalDate from, LocalDate to) {
        ValidUserAuthorized.validAdminAuthorized(user);

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
}
