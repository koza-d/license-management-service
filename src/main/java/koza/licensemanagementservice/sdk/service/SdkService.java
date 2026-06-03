package koza.licensemanagementservice.sdk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.plan.entity.Plan;
import koza.licensemanagementservice.domain.plan.repository.PlanRepository;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.domain.software.version.entity.SoftwareVersion;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.session.dto.SessionValue;
import koza.licensemanagementservice.domain.session.service.SessionManager;
import koza.licensemanagementservice.sdk.dto.request.*;
import koza.licensemanagementservice.sdk.dto.resposne.HeartbeatData;
import koza.licensemanagementservice.sdk.dto.resposne.HeartbeatResponse;
import koza.licensemanagementservice.sdk.dto.resposne.InitResponse;
import koza.licensemanagementservice.sdk.dto.resposne.VerifyData;
import koza.licensemanagementservice.sdk.dto.resposne.VerifyResponse;
import koza.licensemanagementservice.domain.session.log.entity.ReleaseType;
import koza.licensemanagementservice.sdk.log.dto.InitFailedEvent;
import koza.licensemanagementservice.sdk.log.dto.InitSuccessEvent;
import koza.licensemanagementservice.sdk.log.dto.VerifyFailedEvent;
import koza.licensemanagementservice.sdk.log.dto.VerifySuccessEvent;
import koza.licensemanagementservice.sdk.security.AESEncryption;
import koza.licensemanagementservice.sdk.security.ECDHExchange;
import koza.licensemanagementservice.sdk.security.Ed25519KeyProvider;
import koza.licensemanagementservice.sdk.security.Ed25519Signer;
import koza.licensemanagementservice.sdk.security.HMACSignature;
import koza.licensemanagementservice.sdk.security.SessionKeyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.KeyPair;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static koza.licensemanagementservice.global.util.RequestIPAddressParser.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SdkService {
    private final SoftwareRepository softwareRepository;
    private final LicenseRepository licenseRepository;
    private final PlanRepository planRepository;
    private final SessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final Ed25519KeyProvider ed25519KeyProvider;
    private final ApplicationEventPublisher eventPublisher;

    public InitResponse init(InitRequest request, HttpServletRequest servletRequest) throws Exception {
        String userAgent = servletRequest.getHeader("User-Agent");
        String ipAddress = parseIpAddress(servletRequest);
        Software software = null;

        try {
            String appId = request.getAppId();

            software = softwareRepository.findByAppId(appId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.SDK_INVALID_SOFTWARE));

            validateSoftwareStatus(software);

            SoftwareVersion latestVersion = software.getLatestVersion();
            if (latestVersion == null)
                throw new BusinessException(ErrorCode.SDK_INVALID_SOFTWARE);

            SoftwareVersion clientVersion = software.getVersions().stream()
                    .filter(v -> v.getVersion().equals(request.getClientVersion()) && v.isAvailable())
                    .findAny()
                    .orElseThrow(() -> new BusinessException(ErrorCode.SDK_NOT_AVAILABLE_VERSION,
                            Map.of(
                                    "latestVersion", Optional.ofNullable(latestVersion.getVersion()).orElse(""),
                                    "downloadURL", Optional.ofNullable(latestVersion.getDownloadURL()).orElse("")
                            )
                    ));

            if (StringUtils.hasText(clientVersion.getFileHash()) && !clientVersion.getFileHash().equals(request.getFileHash())) {
                throw new BusinessException(ErrorCode.SDK_INVALID_FILE_HASH);
            }

            String downloadURL = Optional.ofNullable(latestVersion.getDownloadURL()).orElse("");

            long timestamp = System.currentTimeMillis();
            String dataToSign = String.join(".",
                    appId,
                    latestVersion.getVersion(),
                    downloadURL,
                    String.valueOf(timestamp));
            String sig = Ed25519Signer.sign(dataToSign, ed25519KeyProvider.getPrivateKey());

            InitResponse response = InitResponse.builder()
                    .softwareName(software.getName())
                    .latestVersion(latestVersion.getVersion())
                    .clientVersion(request.getClientVersion())
                    .downloadURL(downloadURL)
                    .sig(sig)
                    .ts(String.valueOf(timestamp))
                    .build();

            eventPublisher.publishEvent(new InitSuccessEvent(software.getId(), appId, request.getClientVersion(), ipAddress, userAgent));
            return response;
        } catch (Exception e) {
            ErrorCode errorCode = e instanceof BusinessException
                    ? ((BusinessException) e).getError()
                    : ErrorCode.SDK_SERVER_ERROR;
            eventPublisher.publishEvent(new InitFailedEvent(
                    software != null ? software.getId() : null,
                    request.getAppId(),
                    request.getClientVersion(),
                    errorCode.getCode(),
                    ipAddress, userAgent));
            throw e;
        }
    }

    @Transactional
    public VerifyResponse verify(VerifyRequest request, HttpServletRequest servletRequest) throws Exception {
        String userAgent = servletRequest.getHeader("User-Agent");
        String ipAddress = parseIpAddress(servletRequest);

        Software software = null;
        License license = null;

        try {
            String licenseKey = request.getLicenseKey();
            String appId = request.getAppId();
            String fileHash = request.getFileHash();

            software = softwareRepository.findByAppId(appId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.SDK_INVALID_SOFTWARE));

            license = licenseRepository.findByLicenseKeyWithSoftware(licenseKey)
                    .orElseThrow(() -> new BusinessException(ErrorCode.SDK_INVALID_LICENSE));

            // 소프트웨어에 속한 라이센스가 아닌 경우
            if (!software.getId().equals(license.getSoftware().getId()))
                throw new BusinessException(ErrorCode.SDK_INVALID_LICENSE);

            validateSoftwareStatus(software);

            switch (license.getStatus()) {
                case BANNED -> {
                    HashMap<Object, Object> data = new HashMap<>();
                    if (license.getStatusUntil() != null)
                        data.put("until", license.getStatusUntil());
                    data.put("reason", Optional.ofNullable(license.getStatusReason()).orElse("-"));

                    throw new BusinessException(ErrorCode.SDK_LICENSE_BANNED, data);
                }
                case EXPIRED -> throw new BusinessException(ErrorCode.SDK_LICENSE_EXPIRED);
            }

            SoftwareVersion latestVersion = software.getLatestVersion();
            if (latestVersion == null)
                throw new BusinessException(ErrorCode.SDK_INVALID_SOFTWARE);

            SoftwareVersion clientVersion = software.getVersions().stream()
                    .filter(v -> v.getVersion().equals(request.getClientVersion()) && v.isAvailable())
                    .findAny()
                    .orElseThrow(() -> new BusinessException(ErrorCode.SDK_NOT_AVAILABLE_VERSION,
                            Map.of(
                                    "latestVersion", Optional.ofNullable(latestVersion.getVersion()).orElse(""),
                                    "downloadURL", Optional.ofNullable(latestVersion.getDownloadURL()).orElse("")
                            )
                    ));

            if (StringUtils.hasText(clientVersion.getFileHash()) && !clientVersion.getFileHash().equals(fileHash))
                throw new BusinessException(ErrorCode.SDK_INVALID_FILE_HASH);

            // 첫 인증인 라이센스는 플랜 한도 검증 필요
            if (license.getStatus() == LicenseStatus.INACTIVE) {
                Member member = license.getSoftware().getMember();
                long allocatedLicenses = licenseRepository.countAllocatedLicenses(member.getId());
                Plan userPlan = planRepository.findByPlanCode(member.getCurrentPlanCode())
                        .orElseThrow(() -> new BusinessException(ErrorCode.SDK_SERVER_ERROR));

                // 라이센스 활성 한도 제한
                if (allocatedLicenses >= userPlan.getLimitLicense())
                    throw new BusinessException(ErrorCode.SDK_LICENSE_ACTIVE_LIMIT);
            }

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

            String sessionId = sessionManager.createSession(license, ipAddress, userAgent, license.getExpiredAt(), sessionKey);
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
                    .latestVersion(latestVersion.getVersion())
                    .downloadURL(latestVersion.getDownloadURL())
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
                : ErrorCode.SDK_SERVER_ERROR;

        if (errorCode == ErrorCode.SDK_SERVER_ERROR)
            log.error("SDK 인증 도중 문제가 발생했습니다. | 요청객체 : {} | 라이센스 ID : {} | 요청 IP : {} | UserAgent : {} | 사유 : {} ",
                    request.toString(), license == null ? "null" : license.getId(), ipAddress, userAgent, e.getMessage());

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
                .orElseThrow(() -> new BusinessException(ErrorCode.SDK_SESSION_EXPIRED));

        byte[] currentSessionKey = sessionValue.getSessionKey();
        byte[] signingKey = SessionKeyManager.deriveSigningKey(currentSessionKey);
        byte[] encryptKey = SessionKeyManager.deriveEncryptKey(currentSessionKey);


        // 30초 이상 된 요청은 리플레이 공격으로 간주
        long nowTs = System.currentTimeMillis();
        Long receivedTs = request.getReceivedTs();
        if (Math.abs(nowTs - receivedTs) > 30 * 1000)
            throw new BusinessException(ErrorCode.SDK_INVALID_REQUEST);

        // 서명 검증 (위, 변조된 요청 검증)
        String signTarget = sessionId + "." + receivedTs;
        if (!HMACSignature.verify(signTarget, request.getReceivedSig(), signingKey))
            throw new BusinessException(ErrorCode.SDK_INVALID_REQUEST);

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

    public void changeLocalVariable(ChangeLocalVariablesRequest request) throws Exception {
        String sessionId = request.getSessionId();
        SessionValue sessionValue = sessionManager.getSession(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SDK_SESSION_EXPIRED));

        String key = request.getKey();
        String value = request.getValue();

        byte[] signingKey = SessionKeyManager.deriveSigningKey(sessionValue.getSessionKey());
        String signTarget = sessionId + "." + key + "." + value;
        if (!HMACSignature.verify(signTarget, request.getReceivedSig(), signingKey))
            throw new BusinessException(ErrorCode.SDK_INVALID_REQUEST);

        int maxKeyLength = 50;
        int maxValueLength = 500;
        int maxVariableCount = 30;
        if (key == null || value == null)
            throw new BusinessException(ErrorCode.SDK_VARIABLE_NULL);

        if (key.length() > maxKeyLength)
            throw new BusinessException(ErrorCode.SDK_VARIABLE_KEY_MAX);

        if (value.length() > maxValueLength)
            throw new BusinessException(ErrorCode.SDK_VARIABLE_VALUE_MAX);

        Map<String, String> variables = sessionValue.getChangedLocalVariables();
        if (variables.size() >= maxVariableCount)
            throw new BusinessException(ErrorCode.SDK_VARIABLE_COUNT_MAX);

        variables.put(key, value);
        sessionManager.updateSession(sessionId, sessionValue);
    }

    @Transactional
    public void release(ReleaseRequest request) {
        String sessionId = request.getSessionId();
        SessionValue sessionValue = sessionManager.getSession(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SDK_SESSION_EXPIRED));
        processRelease(sessionId, sessionValue.getLicenseId(), ReleaseType.NORMAL);
    }


    @Transactional
    public void revokeExpire(String sessionId) { // 만료된 세션 처리
        SessionValue sessionValue = sessionManager.getSession(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SDK_SESSION_EXPIRED));
        processRelease(sessionId, sessionValue.getLicenseId(), ReleaseType.TIMEOUT);
    }

    private void processRelease(String sessionId, Long licenseId, ReleaseType releaseType) {
        License license = licenseRepository.findById(licenseId).orElseGet(() -> {
            log.warn("세션에 저장된 라이센스 ID가 잘못됐습니다. SessionId: {}", sessionId);
            return null;
        });
        if (license == null) return;

        sessionManager.releaseSession(sessionId, license, releaseType);
    }

    private void validateSoftwareStatus(Software software) {
        switch (software.getStatus()) {
            case BANNED -> {
                HashMap<Object, Object> data = new HashMap<>();
                if (software.getStatusUntil() != null)
                    data.put("until", software.getStatusUntil());
                data.put("reason", Optional.ofNullable(software.getStatusReason()).orElse("-"));
                throw new BusinessException(ErrorCode.SDK_SOFTWARE_BANNED, data);
            }
            case INACTIVE -> throw new BusinessException(ErrorCode.SDK_SOFTWARE_INACTIVE);
            case MAINTENANCE -> {
                HashMap<Object, Object> data = new HashMap<>();
                if (software.getStatusUntil() != null)
                    data.put("until", software.getStatusUntil());
                data.put("reason", Optional.ofNullable(software.getStatusReason()).orElse("-"));
                throw new BusinessException(ErrorCode.SDK_SOFTWARE_MAINTENANCE, data);
            }
            case UNSUPPORTED -> throw new BusinessException(ErrorCode.SDK_SOFTWARE_UNSUPPORTED,
                    Map.of("reason", software.getStatusReason()));
        }
    }
}
