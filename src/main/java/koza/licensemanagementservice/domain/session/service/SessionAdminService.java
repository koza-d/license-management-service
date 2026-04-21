package koza.licensemanagementservice.domain.session.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.session.dto.SessionValue;
import koza.licensemanagementservice.domain.session.repository.SessionSearchCondition;
import koza.licensemanagementservice.domain.session.dto.request.SessionTerminateRequest;
import koza.licensemanagementservice.domain.session.dto.request.SessionTerminationsBulkRequest;
import koza.licensemanagementservice.domain.session.dto.response.SessionAdminDetailResponse;
import koza.licensemanagementservice.domain.session.dto.response.SessionAdminResponse;
import koza.licensemanagementservice.domain.session.dto.response.SessionBulkTerminationResponse;
import koza.licensemanagementservice.domain.session.log.dto.SessionBulkTerminatedEvent;
import koza.licensemanagementservice.domain.session.log.dto.SessionTerminatedEvent;
import koza.licensemanagementservice.domain.session.log.entity.ReleaseType;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionAdminService {
    private final LicenseRepository licenseRepository;
    private final SessionManager sessionManager;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<SessionAdminResponse> getSessions(SessionSearchCondition condition, Pageable pageable) {
        Page<SessionAdminResponse> result = licenseRepository.findActiveSessionLicensesByCondition(condition, pageable);
        List<SessionAdminResponse> content = result
                .filter(response -> sessionManager.getSessionByLicenseId(response.getLicenseId()).isPresent())
                .map(response -> {
                    SessionValue session = sessionManager.getSessionByLicenseId(response.getLicenseId())
                            .orElse(SessionValue.builder().sessionId("-").build());
                    response.setSessionId(session.getSessionId());
                    response.setIpAddress(session.getIpAddress());
                    return response;
                })
                .stream().skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, result.getContent().size());
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
}
