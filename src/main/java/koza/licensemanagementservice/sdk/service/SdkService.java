package koza.licensemanagementservice.sdk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.session.dto.SessionValue;
import koza.licensemanagementservice.domain.session.service.SessionManager;
import koza.licensemanagementservice.sdk.dto.request.HeartbeatRequest;
import koza.licensemanagementservice.sdk.dto.request.ReleaseRequest;
import koza.licensemanagementservice.sdk.dto.request.VerifyRequest;
import koza.licensemanagementservice.sdk.dto.resposne.HeartbeatData;
import koza.licensemanagementservice.sdk.dto.resposne.HeartbeatResponse;
import koza.licensemanagementservice.sdk.dto.resposne.VerifyData;
import koza.licensemanagementservice.sdk.dto.resposne.VerifyResponse;
import koza.licensemanagementservice.domain.session.log.entity.ReleaseType;
import koza.licensemanagementservice.domain.session.log.repository.SessionLogRepository;
import koza.licensemanagementservice.sdk.log.dto.VerifyFailedEvent;
import koza.licensemanagementservice.sdk.log.dto.VerifySuccessEvent;
import koza.licensemanagementservice.sdk.security.AESEncryption;
import koza.licensemanagementservice.sdk.security.ECDHExchange;
import koza.licensemanagementservice.sdk.security.HMACSignature;
import koza.licensemanagementservice.sdk.security.SessionKeyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.time.Duration;
import java.time.LocalDateTime;

import static koza.licensemanagementservice.global.util.RequestIPAddressParser.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SdkService {
    private final SoftwareRepository softwareRepository;
    private final LicenseRepository licenseRepository;
    private final SessionManager sessionManager;
    private final SessionLogRepository sessionLogRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public VerifyResponse verify(VerifyRequest request, HttpServletRequest servletRequest) throws Exception {
        String userAgent = servletRequest.getHeader("User-Agent");
        String ipAddress = parseIpAddress(servletRequest);

        Software software = null;
        License license = null;

        try {
            String licenseKey = request.getLicenseKey();
            String appId = request.getAppId();
            software = softwareRepository.findByAppId(appId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.SOFTWARE_NOT_FOUND));

            license = licenseRepository.findByLicenseKeyWithSoftware(licenseKey)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_LICENSE));

            if (!software.getId().equals(license.getSoftware().getId()))
                throw new BusinessException(ErrorCode.NOT_FOUND_LICENSE);

            // 라이센스 만료 시
            if (license.getExpiredAt().isBefore(LocalDateTime.now()))
                throw new BusinessException(ErrorCode.EXPIRED_LICENSE);

            String currentSessionId = sessionManager.getSessionIdByLicenseId(license.getId());

            // 사용중인 라이센스인 경우 연결 거부
            if (currentSessionId != null && sessionManager.isActive(currentSessionId))
                throw new BusinessException(ErrorCode.ALREADY_USE_LICENSE);


            String clientPublicKey = request.getPublicKey();

            // 서버 키쌍 생성
            KeyPair serverKeyPair = ECDHExchange.generateServerKeyPair();

            // 공유 비밀키 계산
            byte[] sharedSecret = ECDHExchange.computeSharedSecret(serverKeyPair.getPrivate(), clientPublicKey);

            // 이후 통신에 쓰일 암호화 키 ( 매 하트비트마다 초기화) + 공유 비밀키로 암호화
            byte[] sessionKey = SessionKeyManager.generateSessionKey();
            String encryptedSessionKey = AESEncryption.encrypt(sessionKey, ECDHExchange.deriveEncryptKey(sharedSecret));

            byte[] signingKey = SessionKeyManager.deriveSigningKey(sessionKey);
            byte[] encryptKey = SessionKeyManager.deriveEncryptKey(sessionKey);

            String sessionId = sessionManager.createSession(license.getId(), ipAddress, userAgent, license.getExpiredAt(), sessionKey);

            license.verify();

            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(now, license.getExpiredAt());
            long remainMs = Math.max(0, duration.toMillis());

            VerifyData data = VerifyData.builder()
                    .sessionId(sessionId)
                    .exp(license.getExpiredAt())
                    .serverTime(LocalDateTime.now())
                    .remainMs(remainMs)
                    .localVariables(license.getMergeLocalVariables())
                    .globalVariables(license.getSoftware().getGlobalVariables())
                    .build();

            String dataToJson = objectMapper.writeValueAsString(data);
            String encryptedData = AESEncryption.encrypt(dataToJson, encryptKey);

            // 서명 생성 (encryptedData + timestamp 조합)
            long timestamp = System.currentTimeMillis();
            String signTarget = encryptedData + "." + timestamp;
            String sig = HMACSignature.sign(signTarget, signingKey);

            VerifyResponse response = VerifyResponse.builder()
                    .serverPublicKey(ECDHExchange.exportPublicKey(serverKeyPair.getPublic()))
                    .encryptedSessionKey(encryptedSessionKey)
                    .encryptedData(encryptedData)
                    .sig(sig)
                    .ts(String.valueOf(timestamp))
                    .build();

            eventPublisher.publishEvent(new VerifySuccessEvent(software.getId(), request.getAppId(), license.getId(), licenseKey, ipAddress, userAgent));
            return response;
        } catch (Exception e) {
            publishFailure(request, e, software, license, ipAddress, userAgent);
            throw e;
        }
    }

    private void publishFailure(VerifyRequest request, Exception e, Software software, License license, String ipAddress, String userAgent) {
        ErrorCode errorCode = e instanceof BusinessException
                ? ((BusinessException) e).getError()
                : ErrorCode.INTERNAL_SERVER_ERROR;

        eventPublisher.publishEvent(new VerifyFailedEvent(
                software != null ? software.getId() : null, request.getAppId(),
                license != null ? license.getId() : null, request.getLicenseKey(),
                errorCode.getCode(),
                ipAddress,
                userAgent));
    }

    public HeartbeatResponse heartbeat(HeartbeatRequest request) throws Exception {
        String sessionId = request.getSessionId();
        SessionValue sessionValue = sessionManager.getSession(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPIRED_SESSION));

        byte[] currentSessionKey = sessionValue.getSessionKey();
        byte[] signingKey = SessionKeyManager.deriveSigningKey(currentSessionKey);
        byte[] encryptKey = SessionKeyManager.deriveEncryptKey(currentSessionKey);


        // 30초 이상 된 요청은 리플레이 공격으로 간주
        long nowTs = System.currentTimeMillis();
        Long receivedTs = request.getReceivedTs();
        if (Math.abs(nowTs - receivedTs) > 30 * 1000)
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        // 서명 검증 (위, 변조된 요청 검증)
        String signTarget = sessionId + "." + receivedTs;
        if (!HMACSignature.verify(signTarget, request.getReceivedSig(), signingKey))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        // 새 sessionKey 재발급
        byte[] newSessionKey = SessionKeyManager.generateSessionKey();
        byte[] newSigningKey = SessionKeyManager.deriveSigningKey(newSessionKey);
        byte[] newEncryptKey = SessionKeyManager.deriveEncryptKey(newSessionKey);
        String encryptedNewSessionKey = AESEncryption.encrypt(
                newSessionKey,
                encryptKey
        );

        sessionManager.extendSession(sessionId, newSessionKey);

        // 응답 데이터 구성 및 암호화/서명
        HeartbeatData data = new HeartbeatData(LocalDateTime.now(), sessionValue.getExpiredAt());
        String dataToJson = objectMapper.writeValueAsString(data);
        String encryptedData = AESEncryption.encrypt(dataToJson, newEncryptKey);
        long timestamp = System.currentTimeMillis();
        String sig = HMACSignature.sign(encryptedData + "." + timestamp, newSigningKey);

        return HeartbeatResponse.builder()
                .encryptedSessionKey(encryptedNewSessionKey)
                .encryptedData(encryptedData)
                .sig(sig)
                .ts(String.valueOf(timestamp))
                .build();
    }

    @Transactional
    public void release(ReleaseRequest request) {
        String sessionId = request.getSessionId();
        SessionValue sessionValue = sessionManager.getSession(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPIRED_SESSION));
        processRelease(sessionId, sessionValue.getLicenseId(), ReleaseType.NORMAL);
    }


    @Transactional
    public void revokeExpire(String sessionId) { // 만료된 세션 처리
        SessionValue sessionValue = sessionManager.getSession(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPIRED_SESSION));
        processRelease(sessionId, sessionValue.getLicenseId(), ReleaseType.TIMEOUT);
    }

    private void processRelease(String sessionId, Long licenseId, ReleaseType releaseType) {
        License license = licenseRepository.findById(licenseId).orElseGet(() -> {
            log.warn("세션에 저장된 라이센스 ID가 잘못됐습니다. SessionId: {}", sessionId);
            return null;
        });
        if (license == null) return;

        license.release();
        sessionManager.releaseSession(sessionId, license, releaseType);
    }
}
