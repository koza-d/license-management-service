package koza.licensemanagementservice.domain.software.log.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.log.dto.response.SoftwareLogResponse;
import koza.licensemanagementservice.domain.software.log.repository.SoftwareLogRepository;
import koza.licensemanagementservice.domain.software.log.dto.condition.SoftwareLogSearchCondition;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SoftwareLogService {
    private final SoftwareRepository softwareRepository;
    private final SoftwareLogRepository logRepository;

    @Transactional(readOnly = true)
    public Page<SoftwareLogResponse> getSoftwareLogs(CustomUser user, Long softwareId, SoftwareLogSearchCondition condition, Pageable pageable) {
        Software software = softwareRepository.findByIdWithMember(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!software.getMember().getId().equals(user.getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        if (condition.getFrom() != null && condition.getTo() != null
                && condition.getFrom().isAfter(condition.getTo()))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        return logRepository.findBySoftwareId(softwareId, condition, pageable);
    }
}
