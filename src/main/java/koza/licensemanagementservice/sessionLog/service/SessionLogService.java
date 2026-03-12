package koza.licensemanagementservice.sessionLog.service;

import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.license.entity.License;
import koza.licensemanagementservice.license.repository.LicenseRepository;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.sessionLog.dto.DailyUsageResponse;
import koza.licensemanagementservice.sessionLog.dto.SessionHistoryResponse;
import koza.licensemanagementservice.sessionLog.repository.SessionLogRepository;
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
        getLicenseOrThrow(user, licenseId);
        LocalDateTime startDate = LocalDateTime.now().minusDays(range);
        return logRepository.findDailyUsage(licenseId, startDate)
                .stream().map(DailyUsageResponse::from).collect(Collectors.toList());
   }

   public Page<SessionHistoryResponse> getLicenseUsageHistory(CustomUser user, Long licenseId, Pageable pageable) {
       getLicenseOrThrow(user, licenseId);

       return logRepository.findByLicenseId(licenseId, pageable)
               .map(SessionHistoryResponse::from);
   }

    private void getLicenseOrThrow(CustomUser user, Long licenseId) {
        License license = licenseRepository.findByIdWithSoftwareAndMember(licenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!user.getId().equals(license.getSoftware().getMember().getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
}
