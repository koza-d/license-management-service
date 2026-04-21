package koza.licensemanagementservice.domain.session.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.session.dto.SessionValue;
import koza.licensemanagementservice.domain.session.dto.request.SessionSearchCondition;
import koza.licensemanagementservice.domain.session.dto.request.SessionTerminateRequest;
import koza.licensemanagementservice.domain.session.dto.request.SessionTerminationsBulkRequest;
import koza.licensemanagementservice.domain.session.dto.response.SessionAdminDetailResponse;
import koza.licensemanagementservice.domain.session.dto.response.SessionAdminListResponse;
import koza.licensemanagementservice.domain.session.dto.response.SessionBulkTerminationResponse;
import koza.licensemanagementservice.domain.session.log.dto.SessionBulkTerminatedEvent;
import koza.licensemanagementservice.domain.session.log.dto.SessionTerminatedEvent;
import koza.licensemanagementservice.domain.session.log.entity.ReleaseType;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionAdminService {
    private final LicenseRepository licenseRepository;
    private final SessionManager sessionManager;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<SessionAdminListResponse> getSessions(SessionSearchCondition condition, Pageable pageable) {
        Pageable resolved = applySortAlias(pageable);
        Page<License> licenses = licenseRepository.findActiveSessionLicensesByCondition(condition, resolved);

        List<SessionAdminListResponse> items = licenses.stream()
                .map(this::toListResponse)
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(items, resolved, licenses.getTotalElements());
    }

    @Transactional(readOnly = true)
    public SessionAdminDetailResponse getSession(String sessionId) {
        SessionValue session = sessionManager.getSession(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        License license = licenseRepository.findByIdWithSoftwareAndMember(session.getLicenseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        return SessionAdminDetailResponse.of(
                session,
                license.getSoftware().getMember().getEmail(),
                license.getSoftware().getName(),
                license.getLicenseKey(),
                license.getName()
        );
    }

    @Transactional
    public void terminate(CustomUser user, String sessionId, SessionTerminateRequest request) {
        SessionValue session = sessionManager.getSession(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        License license = licenseRepository.findByIdWithSoftwareAndMember(session.getLicenseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        if (!license.hasActiveSession()) {
            throw new BusinessException(ErrorCode.SESSION_ALREADY_TERMINATED);
        }

        String reason = request != null ? request.getReason() : null;
        terminateInternal(session.getSessionId(), license, reason);
        eventPublisher.publishEvent(new SessionTerminatedEvent(user.getId(), session.getSessionId(), license.getId(), reason));
    }

    @Transactional
    public SessionBulkTerminationResponse terminateBulk(CustomUser user, SessionTerminationsBulkRequest request) {
        int terminated = 0;
        int failed = 0;
        List<String> terminatedIds = new ArrayList<>();

        for (String sessionId : request.getIds()) {
            try {
                Optional<SessionValue> optSession = sessionManager.getSession(sessionId);
                if (optSession.isEmpty()) {
                    failed++;
                    continue;
                }
                Optional<License> optLicense = licenseRepository.findByIdWithSoftwareAndMember(optSession.get().getLicenseId());
                if (optLicense.isEmpty() || !optLicense.get().hasActiveSession()) {
                    failed++;
                    continue;
                }
                terminateInternal(sessionId, optLicense.get(), request.getReason());
                terminated++;
                terminatedIds.add(sessionId);
            } catch (Exception e) {
                log.warn("세션 일괄 종료 실패: sessionId={}, cause={}", sessionId, e.getMessage());
                failed++;
            }
        }
        eventPublisher.publishEvent(new SessionBulkTerminatedEvent(user.getId(), terminatedIds, terminated, failed, request.getReason()));
        return SessionBulkTerminationResponse.builder()
                .terminated(terminated)
                .failed(failed)
                .build();
    }

    private void terminateInternal(String sessionId, License license, String reason) {
        license.release();
        sessionManager.releaseSession(sessionId, license, ReleaseType.FORCE_CLOSE);
        if (reason != null && !reason.isBlank()) {
            log.info("관리자 세션 종료: sessionId={}, licenseId={}, reason={}", sessionId, license.getId(), reason);
        }
    }

    private SessionAdminListResponse toListResponse(License license) {
        return sessionManager.getSessionByLicenseId(license.getId())
                .map(session -> SessionAdminListResponse.of(
                        session,
                        license.getSoftware().getMember().getEmail(),
                        license.getSoftware().getName(),
                        license.getLicenseKey(),
                        license.getName()))
                .orElse(null);
    }

    /**
     * 스펙상 sort 키는 "startedAt" 이지만 License 엔티티에는 해당 컬럼이 없으므로
     * 세션 시작 시각에 대응되는 license.latestActiveAt 로 치환한다.
     */
    private Pageable applySortAlias(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "latestActiveAt"));
        }

        List<Sort.Order> translated = pageable.getSort().stream()
                .map(o -> "startedAt".equals(o.getProperty())
                        ? new Sort.Order(o.getDirection(), "latestActiveAt")
                        : o)
                .toList();
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(translated));
    }
}
