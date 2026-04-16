package koza.licensemanagementservice.domain.software.log.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.software.log.dto.SoftwareLogResponse;
import koza.licensemanagementservice.domain.software.log.repository.SoftwareLogRepository;
import koza.licensemanagementservice.domain.software.log.repository.SoftwareLogSearchCondition;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.global.validation.ValidUserAuthorized;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static koza.licensemanagementservice.global.validation.ValidUserAuthorized.validAdminAuthorized;

@Service
@RequiredArgsConstructor
public class SoftwareAdminLogService {
    private final SoftwareLogRepository logRepository;
    public Page<SoftwareLogResponse> getSoftwareLogs(CustomUser user, Long softwareId, SoftwareLogSearchCondition condition, Pageable pageable) {
        validAdminAuthorized(user);

        if (condition.getFrom() != null && condition.getTo() != null
                && condition.getFrom().isAfter(condition.getTo()))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        return logRepository.findBySoftwareId(softwareId, condition, pageable);
    }
}
