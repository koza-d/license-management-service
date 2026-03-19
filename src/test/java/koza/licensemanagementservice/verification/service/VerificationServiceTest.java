package koza.licensemanagementservice.verification.service;

import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.session.service.SessionManager;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.verification.dto.request.VerifyRequest;
import koza.licensemanagementservice.verification.dto.resposne.VerifyResponse;
import koza.licensemanagementservice.verification.security.AESEncryption;
import koza.licensemanagementservice.verification.security.ECDHExchange;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VerificationServiceTest {
    @Autowired
    VerificationService verificationService;
    @Autowired
    SessionManager sessionManager;
    @Autowired
    LicenseRepository licenseRepository;

    @Test
    void 동시_verify_요청_성공은_하나만() throws Exception {
        //given
        String licenseKey = "TM72-43BH-6HYQ-GWE4";

        int threadCount = 2;
        CountDownLatch startLatch = new CountDownLatch(1);  // 출발 신호용
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<VerifyResponse> successList = Collections.synchronizedList(new ArrayList<>());
        List<BusinessException> exceptionList = Collections.synchronizedList(new ArrayList<>());

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRemoteAddr("127.0.0.1");
        mockRequest.addHeader("User-Agent", "test-agent");

        KeyPair clientKeyPair = ECDHExchange.generateServerKeyPair();
        String clientPublicKey = ECDHExchange.exportPublicKey(clientKeyPair.getPublic());
        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    VerifyRequest req = new VerifyRequest(clientPublicKey, licenseKey);
                    VerifyResponse response = verificationService.verify(req, mockRequest);
                    successList.add(response);
                } catch (BusinessException e) {
                    exceptionList.add(e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        startLatch.countDown();  // 모든 스레드 동시 출발
        latch.await();

        // then
        assertThat(successList).hasSize(1);
        assertThat(exceptionList).hasSize(1);
        assertThat(exceptionList.get(0).getError()).isEqualTo(ErrorCode.ALREADY_USE_LICENSE);
    }
}