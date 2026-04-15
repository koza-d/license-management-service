package koza.licensemanagementservice.domain.software.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareAdminDetailResponse;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareAdminStatsResponse;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareAdminSummaryResponse;
import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import koza.licensemanagementservice.domain.software.log.dto.SoftwareStatusChangedEvent;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.software.dto.request.SoftwareStatusChangeRequest;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.repository.SoftwareAdminSearchCondition;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SoftwareAdminService {
    private final SoftwareRepository softwareRepository;
    private final MemberRepository memberRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void changeStatus(CustomUser user, Long softwareId, SoftwareStatusChangeRequest request) {
        validAdminAuthorized(user);

        Software software = softwareRepository.findById(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOFTWARE_NOT_FOUND));

        Member operator = memberRepository.getReferenceById(user.getId());

        if (software.getStatus() == request.getStatus()) {
            throw new BusinessException(ErrorCode.SOFTWARE_STATUS_SAME);
        }

        SoftwareStatus beforeStatus = software.getStatus();
        eventPublisher.publishEvent(new SoftwareStatusChangedEvent(operator, software, beforeStatus, request.getStatus(), request.getReason()));
        software.changeStatus(request.getStatus());
    }

    public Page<SoftwareAdminSummaryResponse> getSoftwareList(CustomUser user, SoftwareAdminSearchCondition condition, Pageable pageable) {
        validAdminAuthorized(user);

        return softwareRepository.searchSoftwareByCondition(condition, pageable);
    }

    public SoftwareAdminDetailResponse getSoftwareDetail(CustomUser user, Long softwareId) {
        validAdminAuthorized(user);
        return softwareRepository.findBySoftwareId(softwareId);
    }

    public SoftwareAdminStatsResponse getSoftwareStats(CustomUser user, Long softwareId) {
        validAdminAuthorized(user);
        return softwareRepository.getSoftwareUsageStat(softwareId);
    }

    private static void validAdminAuthorized(CustomUser user) {
        user.getAuthorities().stream()
                .filter(auth -> auth.toString().equals("ROLE_ADMIN"))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCESS_DENIED));
    }

}
