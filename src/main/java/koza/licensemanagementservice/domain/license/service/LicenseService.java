package koza.licensemanagementservice.domain.license.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import koza.licensemanagementservice.domain.license.dto.request.LicenseExtendRequest;
import koza.licensemanagementservice.domain.license.dto.request.LicenseIssueRequest;
import koza.licensemanagementservice.domain.license.dto.request.LicenseStatusUpdateRequest;
import koza.licensemanagementservice.domain.license.dto.request.LicenseUpdateRequest;
import koza.licensemanagementservice.domain.license.dto.response.LicenseDetailResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseExtendResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseIssueResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseSummaryResponse;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.session.dto.SessionValue;
import koza.licensemanagementservice.domain.session.service.SessionManager;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LicenseService {
    private final SoftwareRepository softwareRepository;
    private final LicenseRepository licenseRepository;
    private final SessionManager sessionManager;
    private final ObjectMapper objectMapper;

    @Transactional
    public LicenseIssueResponse issueLicense(CustomUser user, LicenseIssueRequest request) {
        // 라이센스 발급
        Long softwareId = request.getSoftwareId();
        Software software = getSoftwareOrThrow(user, softwareId);

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
        return LicenseIssueResponse.from(save);
    }

    @Transactional(readOnly = true)
    public LicenseDetailResponse getLicenseDetail(CustomUser user, Long licenseId) {
        // 라이센스 상세조회
        License license = getLicenseOrThrow(user, licenseId);

        Map<String, Object> finalVars = license.getMergeLocalVariables();

        Optional<SessionValue> sessionOptional = sessionManager.getSessionByLicenseId(licenseId);
        LocalDateTime latestActiveAt = license.getLatestActiveAt();
        if (sessionOptional.isPresent())
            latestActiveAt = sessionOptional.get().getLatestActiveAt();

        return LicenseDetailResponse.of(license, latestActiveAt, finalVars);
    }

    @Transactional(readOnly = true)
    public Page<LicenseSummaryResponse> getLicenseSummaryAll(CustomUser user, String search, Boolean hasActiveSession, Integer expireWithin, Pageable pageable) {
        // 소프트웨어 별 라이센스 목록
        return licenseRepository.findByMemberId(user.getId(), search, hasActiveSession, expireWithin, pageable)
                .map(license -> {
                    Optional<SessionValue> sessionOptional = sessionManager.getSessionByLicenseId(license.getId());
                    LocalDateTime latestActiveAt = license.getLatestActiveAt();
                    if (sessionOptional.isPresent())
                        latestActiveAt = sessionOptional.get().getLatestActiveAt();

                    return LicenseSummaryResponse.of(license, latestActiveAt);
                });
    }

    @Transactional(readOnly = true)
     public Page<LicenseSummaryResponse> getLicenseSummaryBySoftware(CustomUser user, Long softwareId, String search, Boolean hasActiveSession, Pageable pageable) {
        // 소프트웨어 별 라이센스 목록
        getSoftwareOrThrow(user, softwareId);
        return licenseRepository.findBySoftwareId(softwareId, search, hasActiveSession, pageable)
                .map(license -> {
                    Optional<SessionValue> sessionOptional = sessionManager.getSessionByLicenseId(license.getId());
                    LocalDateTime latestActiveAt = license.getLatestActiveAt();
                    if (sessionOptional.isPresent())
                        latestActiveAt = sessionOptional.get().getLatestActiveAt();

                    return LicenseSummaryResponse.of(license, latestActiveAt);
                });
    }

    @Transactional
    public List<LicenseExtendResponse> extendLicense(CustomUser user, Long softwareId, LicenseExtendRequest request) {
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
                .map(license -> LicenseExtendResponse.of(license, request.getDays()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LicenseSummaryResponse> getPreviewExtendLicense(CustomUser user, List<Long> ids) {
        // 연장 시 선택된 라이센스 확인용
        List<License> targetLicenses = licenseRepository.findByIdInWithSoftwareWithMember(ids);

        // 존재하지 않는 라이센스를 request에 담았을 때 NOT FOUND
        if (ids.size() != targetLicenses.size())
            throw new BusinessException(ErrorCode.NOT_FOUND);

        targetLicenses.forEach(license -> {
            if (!license.getSoftware().getMember().getId().equals(user.getId()))
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
        });
        return targetLicenses.stream().map(license -> {
                    Optional<SessionValue> sessionOptional = sessionManager.getSessionByLicenseId(license.getId());
                    LocalDateTime latestActiveAt = license.getLatestActiveAt();
                    if (sessionOptional.isPresent())
                        latestActiveAt = sessionOptional.get().getLatestActiveAt();

                    return LicenseSummaryResponse.of(license, latestActiveAt);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateLicense(CustomUser user, Long licenseId, LicenseUpdateRequest request) {
        License license = getLicenseOrThrow(user, licenseId);

        license.updateName(request.getName());
        license.updateMemo(request.getMemo());
        license.updateLocalVariables(request.getLocalVariables());
    }

    @Transactional
    public void changeStatus(CustomUser user, Long licenseId, LicenseStatusUpdateRequest request) {
        License license = getLicenseOrThrow(user, licenseId);

        String statusString = request.getStatus();
        try {
            LicenseStatus status = LicenseStatus.valueOf(statusString);
            license.changeStatus(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private License getLicenseOrThrow(CustomUser user, Long licenseId) {
        // 요청자에게 라이센스 접근 권한 확인용
        License license = licenseRepository.findByIdWithSoftwareAndMember(licenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 요청자가 라이센스 상위 소프트웨어 주인이 아니면 접근불가
        if (!license.getSoftware().getMember().getId().equals(user.getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        return license;
    }

    private Software getSoftwareOrThrow(CustomUser requestUser, Long targetSoftwareId) {
        // 요청자에게 소프트웨어 접근 권한 확인용
        Software software = softwareRepository.findByIdWithMember(targetSoftwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 요청자가 소프트웨어의 주인이 아니면 접근불가
        if (!software.getMember().getId().equals(requestUser.getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        return software;
    }
}
