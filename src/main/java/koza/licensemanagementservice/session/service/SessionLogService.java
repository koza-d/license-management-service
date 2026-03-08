package koza.licensemanagementservice.session.service;

import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.license.entity.License;
import koza.licensemanagementservice.license.repository.LicenseRepository;
import koza.licensemanagementservice.member.dto.CustomUser;
import koza.licensemanagementservice.session.dto.DailyUsageResponse;
import koza.licensemanagementservice.session.dto.SessionHistoryResponse;
import koza.licensemanagementservice.session.repository.SessionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionLogService {
    private final LicenseRepository licenseRepository;
    private final SessionLogRepository logRepository;

    public List<DailyUsageResponse> getDailyUsageTime(CustomUser user, Long licenseId, int range) {
        License license = licenseRepository.findByIdWithSoftwareAndMember(licenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!user.getId().equals(license.getSoftware().getMember().getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        LocalDateTime startDate = LocalDateTime.now().minusDays(range);
        return logRepository.findDailyUsage(licenseId, startDate)
                .stream().map(DailyUsageResponse::from).collect(Collectors.toList());
   }

   public Page<SessionHistoryResponse> getLicenseUsageHistory(CustomUser user, Long licenseId, Pageable pageable) {
       License license = licenseRepository.findByIdWithSoftwareAndMember(licenseId)
               .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

       if (!user.getId().equals(license.getSoftware().getMember().getId()))
           throw new BusinessException(ErrorCode.ACCESS_DENIED);

       return logRepository.findByLicenseId(licenseId, pageable)
               .map(SessionHistoryResponse::from);
   }
}
