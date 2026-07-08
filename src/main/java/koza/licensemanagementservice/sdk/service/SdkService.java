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
import koza.licensemanagementservice.sdk.security.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.AEADBadTagException;
import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
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

            String dataToSign = String.join(".",
                    appId,
                    software.getName(),
                    latestVersion.getVersion(),
                    request.getClientVersion(),
                    downloadURL);
            String sig = Ed25519Signer.sign(dataToSign, ed25519KeyProvider.getPrivateKey());

            InitResponse response = InitResponse.builder()
                    .softwareName(software.getName())
                    .latestVersion(latestVersion.getVersion())
                    .clientVersion(request.getClientVersion())
                    .downloadURL(downloadURL)
                    .sig(sig)
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

            license = licenseRepository.findByLicenseKey(licenseKey)
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
                Member member = software.getMember();
                long allocatedLicenses = licenseRepository.countAllocatedLicenses(member.getId());
                Plan userPlan = planRepository.findByPlanCode(member.getCurrentPlanCode())
                        .orElseThrow(() -> new BusinessException(ErrorCode.SDK_SERVER_ERROR));

                // 라이센스 활성 한도 제한
                if (allocatedLicenses >= userPlan.getLimitLicense())
                    throw new BusinessException(ErrorCode.SDK_LICENSE_ACTIVE_LIMIT);
            }

            byte[] clientPublicKey = Base64.getDecoder().decode(request.getPublicKey());

            // 서버 키쌍 생성
            KeyPair serverKeyPair = ECDHExchange.generateServerKeyPair();
            byte[] serverPublicKey = serverKeyPair.getPublic().getEncoded();

            // 공유 비밀키 계산 (서버 개인키, 클라 공개키)
            byte[] sharedSecret = ECDHExchange.computeSharedSecret(serverKeyPair.getPrivate(), clientPublicKey);

            // 이후 통신에 쓰일 세션 키
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(clientPublicKey);
            outputStream.write(serverPublicKey);
            byte[] salt = outputStream.toByteArray();
            HKDFUtil.SessionKeys sessionKeys = HKDFUtil.deriveSessionKeys(sharedSecret, salt);

            String sessionId = sessionManager.createSession(license, ipAddress, userAgent, license.getExpiredAt(), sessionKeys.keyC2S(), sessionKeys.keyS2C());
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
                    .globalVariables(software.getGlobalVariables())
                    .latestVersion(latestVersion.getVersion())
                    .downloadURL(Optional.ofNullable(latestVersion.getDownloadURL()).orElse(""))
                    .build();


            String dataToJson = objectMapper.writeValueAsString(data);

            Long serverSeq = sessionManager.getSequence(sessionId);
            String encryptedData = AESEncryption.encrypt(serverSeq, dataToJson, sessionKeys.keyS2C());
            String signTarget = String.join(".",
                    Base64.getEncoder().encodeToString(serverPublicKey),
                    request.getClientNonce(),
                    String.valueOf(ed25519KeyProvider.getKeyId()),
                    encryptedData);
            String serverSign = Ed25519Signer.sign(signTarget, ed25519KeyProvider.getPrivateKey());

            VerifyResponse response = VerifyResponse.builder()
                    .serverPublicKey(Base64.getEncoder().encodeToString(serverPublicKey))
                    .clientNonce(request.getClientNonce())
                    .keyId(ed25519KeyProvider.getKeyId())
                    .encryptedData(encryptedData)
                    .serverSign(serverSign)
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
            log.error("SDK 인증 도중 문제가 발생했습니다. | 요청객체 : {} | 라이센스 ID : {} | 요청 IP : {} | UserAgent : {}",
                    request.toString(), license == null ? "null" : license.getId(), ipAddress, userAgent, e);

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

        if (sessionValue.getExpiredAt().isBefore(LocalDateTime.now()))
            throw new BusinessException(ErrorCode.SDK_LICENSE_EXPIRED);

        byte[] keyC2S = sessionValue.getKeyC2S();
        byte[] keyS2C = sessionValue.getKeyS2C();
        String expected = String.join(".", sessionId, request.getClientSeq().toString());
        verifyC2SPayload(request.getClientSeq(), request.getEncryptData(), keyC2S, expected, sessionId);

        sessionManager.extendSession(sessionId);
        Long serverSeq = sessionManager.increaseSequence(sessionId);

        // 응답 데이터 구성 및 암호화/서명
        HeartbeatData data = new HeartbeatData(LocalDateTime.now(), sessionValue.getExpiredAt());
        String dataToJson = objectMapper.writeValueAsString(data);
        String encryptedData = AESEncryption.encrypt(serverSeq, dataToJson, keyS2C);

        return HeartbeatResponse.builder()
                .serverSeq(serverSeq)
                .encryptedData(encryptedData)
                .build();
    }

    public void changeLocalVariable(ChangeLocalVariablesRequest request) throws Exception {
        String sessionId = request.getSessionId();
        SessionValue sessionValue = sessionManager.getSession(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SDK_SESSION_EXPIRED));

        if (sessionValue.getExpiredAt().isBefore(LocalDateTime.now()))
            throw new BusinessException(ErrorCode.SDK_LICENSE_EXPIRED);

        String key = request.getKey();
        String value = request.getValue();

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

        byte[] keyC2S = sessionValue.getKeyC2S();
        String expected = String.join(".",
                sessionId,
                request.getClientSeq().toString(),
                request.getKey(),
                request.getValue());
        verifyC2SPayload(request.getClientSeq(), request.getEncryptData(), keyC2S, expected, sessionId);
        variables.put(key, value);
        sessionManager.updateSession(sessionId, sessionValue);
    }

    @Transactional
    public void release(ReleaseRequest request) {
        String sessionId = request.getSessionId();
        SessionValue sessionValue = sessionManager.getSession(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SDK_SESSION_EXPIRED));

        byte[] keyC2S = sessionValue.getKeyC2S();
        String expected = String.join(".", sessionId, request.getClientSeq().toString());
        verifyC2SPayload(request.getClientSeq(), request.getEncryptData(), keyC2S, expected, sessionId);

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

    private void verifyC2SPayload(Long clientSeq, String encryptData, byte[] keyC2S, String expected, String sessionId) {
        String decrypt;
        try {
            decrypt = AESEncryption.decrypt(clientSeq, encryptData, keyC2S);
        } catch (AEADBadTagException | IllegalArgumentException e) {
            // 정상 루트로는 발생 불가 → 변조/위조/깨진 입력 (클라 문제)
            // - AEADBadTagException: 키, nonce 불일치 또는 encryptData(암호문) 변조
            // - IllegalArgumentException: encryptData가 깨진 Base64(decode 단계에서 터짐)
            throw new BusinessException(ErrorCode.SDK_INVALID_REQUEST);
        } catch (Exception e) {
            log.error("C2S 복호화 서버 오류 sessionId={}", sessionId, e);
            throw new BusinessException(ErrorCode.SDK_SERVER_ERROR);
        }

        if (!expected.equals(decrypt))
            throw new BusinessException(ErrorCode.SDK_INVALID_REQUEST);
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
            case UNSUPPORTED -> {
                throw new BusinessException(ErrorCode.SDK_SOFTWARE_UNSUPPORTED,
                        Map.of("reason", Optional.ofNullable(software.getStatusReason()).orElse("-")));
            }
        }
    }
}
