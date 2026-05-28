package koza.licensemanagementservice.domain.session.log.service;

import koza.licensemanagementservice.domain.session.log.dto.response.DailyUsageResponse;
import koza.licensemanagementservice.domain.session.log.dto.response.SessionHistoryResponse;
import koza.licensemanagementservice.domain.session.log.repository.SessionLogRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.global.util.FillGaps;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionLogService {
    private final LicenseRepository licenseRepository;
    private final SessionLogRepository logRepository;

    public List<DailyUsageResponse> getDailyUsageTime(CustomUser user, Long licenseId, int range) {
        getLicenseOrThrow(user, licenseId);
        LocalDateTime startDate = LocalDateTime.now().minusDays(range);
        List<DailyUsageResponse> result = logRepository.findDailyUsage(licenseId, startDate);

        return FillGaps.fillDateGaps(startDate.toLocalDate(), LocalDate.now(), result,
                DailyUsageResponse::getDate, date -> new DailyUsageResponse(date, 0L)
        );
   }

   public Page<SessionHistoryResponse> getLicenseUsageHistory(CustomUser user, Long licenseId, Pageable pageable) {
       getLicenseOrThrow(user, licenseId);

       return logRepository.findByLicenseId(licenseId, pageable);
   }

    private void getLicenseOrThrow(CustomUser user, Long licenseId) {
        License license = licenseRepository.findByIdWithSoftwareAndMember(licenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!user.getId().equals(license.getSoftware().getMember().getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
}
