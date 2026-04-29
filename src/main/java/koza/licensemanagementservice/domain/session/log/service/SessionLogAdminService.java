package koza.licensemanagementservice.domain.session.log.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.session.log.dto.response.SessionHistoryResponse;
import koza.licensemanagementservice.domain.session.log.repository.SessionLogRepository;
import koza.licensemanagementservice.domain.session.log.dto.condition.SessionLogSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static koza.licensemanagementservice.global.validation.ValidUserAuthorized.validAdminAuthorized;

@Service
@RequiredArgsConstructor
public class SessionLogAdminService {
    private final SessionLogRepository logRepository;


    public Page<SessionHistoryResponse> getLicenseUsageHistory(CustomUser user, Long licenseId, SessionLogSearchCondition condition, Pageable pageable) {
        validAdminAuthorized(user);

        return logRepository.findByLicenseId(licenseId, condition, pageable);
    }
}
