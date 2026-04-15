package koza.licensemanagementservice.domain.session.log.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.session.log.dto.SessionHistoryResponse;
import koza.licensemanagementservice.domain.session.log.repository.SessionLogRepository;
import koza.licensemanagementservice.domain.session.log.repository.SessionLogSearchCondition;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionLogAdminService {
    private final SessionLogRepository logRepository;


    public Page<SessionHistoryResponse> getLicenseUsageHistory(CustomUser user, Long licenseId, SessionLogSearchCondition condition, Pageable pageable) {
        validAdminAuthorized(user);

        return logRepository.findByLicenseId(licenseId, condition, pageable);
    }

    private static void validAdminAuthorized(CustomUser user) {
        user.getAuthorities().stream()
                .filter(auth -> auth.toString().equals("ROLE_ADMIN"))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCESS_DENIED));
    }
}
