package koza.licensemanagementservice.domain.license.service;

import koza.licensemanagementservice.domain.license.dto.condition.LicenseSearchCondition;
import koza.licensemanagementservice.domain.license.dto.request.*;
import koza.licensemanagementservice.domain.license.dto.response.LicenseDetailResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseExtendResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseIssueResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseSummaryResponse;
import koza.licensemanagementservice.domain.license.log.dto.event.LicenseBulkExtendEvent;
import koza.licensemanagementservice.domain.license.log.dto.event.LicenseIssuedEvent;
import koza.licensemanagementservice.domain.license.log.dto.event.LicenseModifiedEvent;
import koza.licensemanagementservice.domain.license.log.dto.event.LicenseStatusChangedEvent;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.plan.entity.Plan;
import koza.licensemanagementservice.domain.plan.repository.PlanRepository;
import koza.licensemanagementservice.domain.session.dto.SessionValue;
import koza.licensemanagementservice.domain.session.service.SessionManager;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final MemberRepository memberRepository;
    private final PlanRepository planRepository;

    private final SessionManager sessionManager;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LicenseIssueResponse issueLicense(CustomUser user, LicenseIssueRequest request) {
        // 라이센스 발급
        Long softwareId = request.getSoftwareId();
        Software software = getSoftwareOrThrow(user, softwareId);
        Member member = software.getMember();

        long allocatedLicenses = licenseRepository.countAllocatedLicenses(user.getId());

        Plan userPlan = planRepository.findByPlanCode(member.getCurrentPlanCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAN_NOT_FOUND));

        // 라이센스 활성/발급 한도 제한
        if (allocatedLicenses >= userPlan.getLimitLicense())
            throw new BusinessException(ErrorCode.LICENSE_ISSUE_LIMIT);

        String licenseKey = LicenseKeyGenerator.generateKey();
        while (licenseRepository.existsByLicenseKey(licenseKey))
            licenseKey = LicenseKeyGenerator.generateKey();

        License license = License.builder()
                .software(software)
                .name(request.getName())
                .memo(request.getMemo())
                .licenseKey(licenseKey)
                .localVariables(request.getLocalVariables())
                .status(LicenseStatus.INACTIVE)
                .startDurationDays(request.getPeriodDays())
                .build();

        License save = licenseRepository.saveAndFlush(license);
        eventPublisher.publishEvent(new LicenseIssuedEvent(save.getId(), user.getId(), save.toSnapshot(), LocalDateTime.now()));
        return LicenseIssueResponse.from(save);
    }

    @Transactional(readOnly = true)
    public LicenseDetailResponse getLicenseDetail(CustomUser user, Long licenseId) {
        // 라이센스 상세조회
        License license = getLicenseOrThrow(user, licenseId);

        Map<String, String> finalVars = license.getMergeLocalVariables();

        Optional<SessionValue> sessionOptional = sessionManager.getSessionByLicenseId(licenseId);
        LocalDateTime latestActiveAt = license.getLatestActiveAt();
        if (sessionOptional.isPresent())
            latestActiveAt = sessionOptional.get().getLatestActiveAt();

        // LicenseDetailResponse.of 내부에서 license.software.versions 을 타고 들어가서 쿼리 1번이 더 나감
        return LicenseDetailResponse.of(license, latestActiveAt, finalVars);
    }

    @Transactional(readOnly = true)
    public Page<LicenseSummaryResponse> searchLicenses(CustomUser user, LicenseSearchCondition condition, Pageable pageable) {
        return licenseRepository.searchLicensesByMemberId(user.getId(), condition, pageable)
                .map(LicenseSummaryResponse::of);
    }

    @Transactional
    public List<LicenseExtendResponse> extendLicense(CustomUser user, LicenseExtendRequest request) {
        // 라이센스 연장
        List<License> targetLicenses = licenseRepository.findByIdInWithSoftwareWithMember(request.getIds());

        // 존재하지 않는 라이센스를 request에 담았을 때
        if (request.getIds().size() != targetLicenses.size())
            throw new BusinessException(ErrorCode.NOT_FOUND);

        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        long allocatedLicenses = licenseRepository.countAllocatedLicenses(member.getId());
        Plan userPlan = planRepository.findByPlanCode(member.getCurrentPlanCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAN_NOT_FOUND));

        int activeDue = 0;
        for (License license : targetLicenses) {
            // 연장하려는 라이센스의 소속 소프트웨어가 본인의 소프트웨어가 아니면 접근 불가
            if (!license.getSoftware().getMember().getId().equals(user.getId()))
                throw new BusinessException(ErrorCode.ACCESS_DENIED);

            if (license.getStatus() == LicenseStatus.INACTIVE)
                throw new BusinessException(ErrorCode.LICENSE_NOT_ACTIVATED);

            if (license.getStatus() == LicenseStatus.EXPIRED)
                activeDue++;

        }
        // 라이센스 활성 한도 제한 - (활성화 된 갯수 + 활성화 될 갯수)
        if (allocatedLicenses + activeDue >= userPlan.getLimitLicense())
            throw new BusinessException(ErrorCode.LICENSE_CANNOT_EXTEND_LIMIT);

        // beforeExpiredAt, afterExpiredAt
        Map<Long, LocalDateTime> beforeExpiredAt = targetLicenses.stream()
                .collect(Collectors.toMap(License::getId, License::getExpiredAt));

        targetLicenses.forEach(license -> license.extendPeriod(request.getDays()));

        Map<Long, LocalDateTime> afterExpiredAt = targetLicenses.stream()
                .collect(Collectors.toMap(License::getId, License::getExpiredAt));
        List<Long> licenseIds = targetLicenses.stream().map(License::getId).collect(Collectors.toList());
        Long periodMs = request.getDays() * 24 * 60 * 60 * 1000L;
        eventPublisher.publishEvent(new LicenseBulkExtendEvent(user.getId(), licenseIds, beforeExpiredAt, afterExpiredAt, periodMs));
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
        return targetLicenses.stream().map(LicenseSummaryResponse::of).toList();
    }

    @Transactional
    public void updateLicense(CustomUser user, Long licenseId, LicenseUpdateRequest request) {
        License license = getLicenseOrThrow(user, licenseId);
        Map<String, Object> before = license.toSnapshot();

        license.updateName(request.getName());
        license.updateMemo(request.getMemo());
        license.updateLocalVariables(request.getLocalVariables());
        Map<String, Object> after = license.toSnapshot();
        eventPublisher.publishEvent(new LicenseModifiedEvent(licenseId, user.getId(), before, after, LocalDateTime.now()));
    }

    @Transactional
    public void ban(CustomUser user, Long licenseId, LicenseBannedRequest request) {
        License license = getLicenseOrThrow(user, licenseId);
        LicenseStatus beforeStatus = license.getStatus();

        LocalDateTime now = LocalDateTime.now();
        int bannedDays = request.getDays();
        LocalDateTime until = bannedDays == 0 ? null : now.plusDays(bannedDays);

        license.changeStatus(LicenseStatus.BANNED, until, request.getReason());
        eventPublisher.publishEvent(new LicenseStatusChangedEvent(licenseId, user.getId(), beforeStatus, LicenseStatus.BANNED, until, request.getReason(), now));
    }

    @Transactional
    public void active(CustomUser user, Long licenseId, LicenseActiveRequest request) {
        License license = getLicenseOrThrow(user, licenseId);
        LicenseStatus beforeStatus = license.getStatus();
        LocalDateTime now = LocalDateTime.now();

        if (beforeStatus == LicenseStatus.EXPIRED)
            throw new BusinessException(ErrorCode.LICENSE_EXPIRED_CANNOT_ACTIVE);

        Member member = license.getSoftware().getMember();

        long allocatedLicenses = licenseRepository.countAllocatedLicenses(member.getId());
        Plan userPlan = planRepository.findByPlanCode(member.getCurrentPlanCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAN_NOT_FOUND));

        // 라이센스 활성 한도 제한 - 영구밴 상태인건 활성불가
        boolean isUnlimitedBan = beforeStatus == LicenseStatus.BANNED && license.getStatusUntil() == null;
        if (allocatedLicenses >= userPlan.getLimitLicense() && (isUnlimitedBan || beforeStatus == LicenseStatus.INACTIVE))
            throw new BusinessException(ErrorCode.LICENSE_CANNOT_ACTIVE_LIMIT);

        if (beforeStatus == LicenseStatus.INACTIVE)
            license.startActive();

        license.changeStatus(LicenseStatus.ACTIVE);
        eventPublisher.publishEvent(new LicenseStatusChangedEvent(licenseId, user.getId(), beforeStatus, LicenseStatus.ACTIVE, request.getReason(), now));
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
