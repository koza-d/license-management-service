package koza.licensemanagementservice.session.service;

import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.license.entity.License;
import koza.licensemanagementservice.license.repository.LicenseRepository;
import koza.licensemanagementservice.member.dto.CustomUser;
import koza.licensemanagementservice.session.dto.SessionResponse;
import koza.licensemanagementservice.session.dto.TerminateBulkRequest;
import koza.licensemanagementservice.session.entity.ReleaseType;
import koza.licensemanagementservice.session.entity.SessionLog;
import koza.licensemanagementservice.session.repository.SessionLogRepository;
import koza.licensemanagementservice.software.entity.Software;
import koza.licensemanagementservice.software.repository.SoftwareRepository;
import koza.licensemanagementservice.session.dto.SessionValue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SoftwareRepository softwareRepository;
    private final LicenseRepository licenseRepository;
    private final SessionManager sessionManager;
    private final SessionLogRepository logRepository;

    @Transactional(readOnly = true)
    public Page<SessionResponse> getAllByMember(CustomUser user, Pageable pageable) {
        Page<License> licenses = licenseRepository.findBySoftware_MemberIdAndHasActiveSessionIsTrue(user.getId(), pageable);
        return getSessionResponses(pageable, licenses);
    }

    @Transactional(readOnly = true)
    public Page<SessionResponse> getBySoftware(CustomUser user, Long softwareId, Pageable pageable) {
        Software software = softwareRepository.findById(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!user.getId().equals(software.getMember().getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        Page<License> licenses = licenseRepository.findBySoftwareIdAndHasActiveSessionIsTrue(softwareId, pageable);

        return getSessionResponses(pageable, licenses);
    }

    @Transactional
    public void terminate(CustomUser user, String sessionId) {
        sessionManager.getSession(sessionId).ifPresent(session -> {
                    licenseRepository.findByIdWithSoftwareAndMember(session.getLicenseId()).ifPresent(license -> {
                        if (!user.getId().equals(license.getSoftware().getMember().getId()))
                            throw new BusinessException(ErrorCode.ACCESS_DENIED);
                        terminateProcess(session, license);
                    });
                }
        );
    }

    @Transactional
    public void terminateBulk(CustomUser user, TerminateBulkRequest request) {
        List<String> sessionIds = request.getIds();
        Map<Long, SessionValue> sessions = new HashMap<>();

        sessionIds.forEach(sessionId -> {
                    sessionManager.getSession(sessionId).ifPresent(
                            session -> sessions.put(session.getLicenseId(), session)
                    );
                }
        );
        licenseRepository.findByIdInWithSoftwareWithMember(sessions.keySet().stream().toList())
                .forEach(license -> {
                            if (!user.getId().equals(license.getSoftware().getMember().getId()))
                                throw new BusinessException(ErrorCode.ACCESS_DENIED);

                            SessionValue session = sessions.get(license.getId());
                            terminateProcess(session, license);
                        }
                );
    }


    @Transactional
    protected void terminateProcess(SessionValue session, License license) {
        if (license == null || session == null)
            return;

        sessionManager.releaseSession(session.getSessionId());
        license.release();

        SessionLog log = SessionLog.builder()
                .sessionId(session.getSessionId())
                .license(license)
                .verifyAt(session.getVerifyAt())
                .releaseAt(LocalDateTime.now())
                .releaseType(ReleaseType.FORCE_CLOSE)
                .build();
        logRepository.save(log);
    }

    private Page<SessionResponse> getSessionResponses(Pageable pageable, Page<License> licenses) {
        List<SessionResponse> responses = licenses
                .map(license -> {
                    Optional<SessionValue> sessionOptional = sessionManager.getSessionByLicenseId(license.getId());
                    if (sessionOptional.isEmpty())
                        return null;

                    SessionValue session = sessionOptional.get();
                    return SessionResponse.of(session, session.getSessionId(),
                            license.getLicenseKey(), license.getName());

                }).filter(Objects::nonNull).toList();
        return new PageImpl<>(responses, pageable, licenses.getTotalElements());
    }
}
