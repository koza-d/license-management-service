package koza.licensemanagementservice.license.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.license.dto.LicenseDTO;
import koza.licensemanagementservice.license.entity.License;
import koza.licensemanagementservice.license.entity.LicenseStatus;
import koza.licensemanagementservice.license.repository.LicenseRepository;
import koza.licensemanagementservice.member.dto.CustomUser;
import koza.licensemanagementservice.software.entity.Software;
import koza.licensemanagementservice.software.repository.SoftwareRepository;
import koza.licensemanagementservice.verification.service.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LicenseService {
    private final SoftwareRepository softwareRepository;
    private final LicenseRepository licenseRepository;
    private final SessionManager sessionManager;
    private final ObjectMapper objectMapper;

    @Transactional
    public LicenseDTO.IssueResponse issueLicense(CustomUser user, LicenseDTO.IssueRequest request) {
        // 라이센스 발급
        Long softwareId = request.getSoftwareId();
        Software software = checkAccessAuthorizedForSoftware(user, softwareId);

        String licenseKey = LicenseKeyGenerator.generateKey();
        while (licenseRepository.existsByLicenseKey(licenseKey))
            licenseKey = LicenseKeyGenerator.generateKey();

        LocalDateTime expireAt = LocalDateTime.now().plusDays(request.getPeriodDays());
        License license = License.builder()
                .software(software)
                .name(request.getName())
                .memo(request.getMemo())
                .licenseKey(licenseKey)
                .expiredAt(expireAt)
                .localVariables(request.getLocalVariables())
                .status(LicenseStatus.ACTIVE)
                .build();

        License save = licenseRepository.saveAndFlush(license);
        return LicenseDTO.IssueResponse.from(save);
    }

    @Transactional(readOnly = true)
    public LicenseDTO.DetailResponse getLicenseDetail(CustomUser user, Long licenseId) {
        // 라이센스 상세조회
        License license = licenseRepository.findByIdWithSoftwareAndMember(licenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 라이센스의 부모인 소프트웨어의 관리자가 아니면 접근불가
        if (!license.getSoftware().getMember().getId().equals(user.getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        Map<String, Object> finalVars = license.getMergeLocalVariables();
        String sessionId = sessionManager.getSessionIdByLicenseId(licenseId);
        LocalDateTime latestActiveAt = sessionManager.getLatestActiveAt(sessionId)
                .orElseGet(license::getLatestActiveAt);

        return LicenseDTO.DetailResponse.of(license, latestActiveAt, finalVars);
    }

    @Transactional(readOnly = true)
    public Page<LicenseDTO.SummaryResponse> getLicenseSummaryBySoftware(CustomUser user, Long softwareId, Pageable pageable) {
        // 소프트웨어 별 라이센스 목록
        checkAccessAuthorizedForSoftware(user, softwareId);

        return licenseRepository.findBySoftwareId(softwareId, pageable)
                .map(LicenseDTO.SummaryResponse::from);
    }

    @Transactional
    public List<LicenseDTO.ExtendResponse> extendLicense(CustomUser user, Long softwareId, LicenseDTO.ExtendRequest request) {
        // 라이센스 연장
        List<License> targetLicenses = licenseRepository.findByIdInWithSoftwareWithMember(request.getIds());

        // 존재하지 않는 라이센스를 request에 담았을 때
        if (request.getIds().size() != targetLicenses.size())
            throw new BusinessException(ErrorCode.NOT_FOUND);

        targetLicenses.forEach(license -> {
            // 연장하려는 라이센스의 소속 소프트웨어가 본인의 소프트웨어가 아니면 접근 불가
            if (!license.getSoftware().getMember().getId().equals(user.getId()))
                throw new BusinessException(ErrorCode.ACCESS_DENIED);

            license.extendPeriod(request.getDays());
        });

        return targetLicenses.stream()
                .map(license -> LicenseDTO.ExtendResponse.of(license, request.getDays()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LicenseDTO.SummaryResponse> getPreviewExtendLicense(CustomUser user, List<Long> ids) {
        // 연장 시 선택된 라이센스 확인용
        List<License> targetLicenses = licenseRepository.findByIdInWithSoftwareWithMember(ids);

        // 존재하지 않는 라이센스를 request에 담았을 때 NOT FOUND
        if (ids.size() != targetLicenses.size())
            throw new BusinessException(ErrorCode.NOT_FOUND);

        targetLicenses.forEach(license -> {
            if (!license.getSoftware().getMember().getId().equals(user.getId()))
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
        });
        return targetLicenses.stream().map(LicenseDTO.SummaryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateLicense(CustomUser user, Long licenseId, LicenseDTO.UpdateRequest request) {
        License license = licenseRepository.findByIdWithSoftwareAndMember(licenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 요청자가 소프트웨어의 주인이 아니면 접근불가
        if (!license.getSoftware().getMember().getId().equals(user.getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        license.updateName(request.getName());
        license.updateMemo(request.getMemo());
        license.updateLocalVariables(request.getLocalVariables());
    }

    private Software checkAccessAuthorizedForSoftware(CustomUser requestUser, Long targetSoftwareId) {
        // 요청자에게 소프트웨어 접근 권한 있는지 확인용
        Software software = softwareRepository.findByIdWithMember(targetSoftwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 요청자가 소프트웨어의 주인이 아니면 접근불가
        if (!software.getMember().getId().equals(requestUser.getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        return software;
    }
}
