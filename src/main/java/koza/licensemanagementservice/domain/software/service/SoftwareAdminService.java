package koza.licensemanagementservice.domain.software.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareAdminSummaryResponse;
import koza.licensemanagementservice.domain.software.log.dto.response.SoftwareStatusLogResponse;
import koza.licensemanagementservice.domain.software.log.entity.SoftwareStatusLog;
import koza.licensemanagementservice.domain.software.log.repository.SoftwareStatusLogRepository;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.software.dto.request.SoftwareStatusChangeRequest;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.repository.SoftwareAdminSearchCondition;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SoftwareAdminService {
    private final SoftwareRepository softwareRepository;
    private final SoftwareStatusLogRepository softwareStatusLogRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void changeStatus(CustomUser admin, Long softwareId, SoftwareStatusChangeRequest request) {
        Software software = softwareRepository.findById(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOFTWARE_NOT_FOUND));
        Member manager = memberRepository.findById(admin.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (software.getStatus() == request.getStatus()) {
            throw new BusinessException(ErrorCode.SOFTWARE_STATUS_SAME);
        }

        SoftwareStatusLog log = SoftwareStatusLog.builder()
                .software(software)
                .manager(manager)
                .previousStatus(software.getStatus())
                .newStatus(request.getStatus())
                .reason(request.getReason())
                .build();
        softwareStatusLogRepository.save(log);

        software.changeStatus(request.getStatus());
    }

    public Page<SoftwareAdminSummaryResponse> getSoftwareList(CustomUser user, SoftwareAdminSearchCondition condition, Pageable pageable) {
        validAdminAuthorized(user);

        return softwareRepository.searchSoftwareByCondition(condition, pageable);
    }
    public List<SoftwareStatusLogResponse> getStatusLogs(Long softwareId) {
        return softwareStatusLogRepository.findBySoftwareIdOrderByCreateAtDesc(softwareId)
                .stream()
                .map(SoftwareStatusLogResponse::from)
                .toList();
    }

    private static void validAdminAuthorized(CustomUser user) {
        user.getAuthorities().stream()
                .filter(auth -> auth.toString().equals("ROLE_ADMIN"))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCESS_DENIED));
    }

}
