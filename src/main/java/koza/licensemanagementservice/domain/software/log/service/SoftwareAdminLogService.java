package koza.licensemanagementservice.domain.software.log.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.software.log.dto.SoftwareLogResponse;
import koza.licensemanagementservice.domain.software.log.repository.SoftwareLogRepository;
import koza.licensemanagementservice.domain.software.log.repository.SoftwareLogSearchCondition;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SoftwareAdminLogService {
    private final SoftwareLogRepository logRepository;
    public Page<SoftwareLogResponse> getSoftwareLogs(CustomUser user, Long softwareId, SoftwareLogSearchCondition condition, Pageable pageable) {
        validAdminAuthorized(user);

        return logRepository.findBySoftwareId(softwareId, condition, pageable);
    }

    private static void validAdminAuthorized(CustomUser user) {
        user.getAuthorities().stream()
                .filter(auth -> auth.toString().equals("ROLE_ADMIN"))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCESS_DENIED));
    }
}
