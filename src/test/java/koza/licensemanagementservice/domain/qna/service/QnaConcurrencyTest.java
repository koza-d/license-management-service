package koza.licensemanagementservice.domain.qna.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.qna.dto.request.QnaAnswerRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaCreateRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaStatusUpdateRequest;
import koza.licensemanagementservice.domain.qna.dto.response.QnaDetailResponse;
import koza.licensemanagementservice.domain.qna.entity.QnaQuestion;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import koza.licensemanagementservice.domain.qna.repository.QnaQuestionRepository;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("tc")
class QnaConcurrencyTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private QnaService qnaService;

    @Autowired
    private QnaQuestionRepository qnaQuestionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SoftwareRepository softwareRepository;

    private Member member;
    private Software software;

    @BeforeEach
    void setUp() {
        qnaQuestionRepository.deleteAllInBatch();
        softwareRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(
                Member.builder().email("concurrent@test.com").password("pw").nickname("동시성테스터").build()
        );
        software = softwareRepository.save(
                Software.builder().member(member).name("ConcurrentApp").latestVersion("1.0").apiKey("ckey").limitLicense(100).build()
        );
    }

    private CustomUser userOf(Member m) {
        return new CustomUser(m.getEmail(), m.getId(), m.getNickname(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private CustomUser adminUser() {
        return new CustomUser("admin@test.com", 999L, "관리자",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    // === 1. 동시 문의 등록 ===

    @Test
    @DisplayName("50명이 동시에 문의 등록 - 모두 정상 저장")
    void concurrent_createQuestion() throws InterruptedException {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    CustomUser user = userOf(member);
                    QnaCreateRequest request = new QnaCreateRequest(
                            software.getId(), "동시 문의 " + idx, "내용 " + idx);
                    qnaService.createQuestion(user, request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("[등록 실패] thread-" + idx + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        long elapsed = System.currentTimeMillis() - start;

        long totalInDb = qnaQuestionRepository.count();
        System.out.println("[동시 등록] 성공: " + successCount + ", 실패: " + failCount
                + ", DB 저장: " + totalInDb + ", 소요: " + elapsed + "ms");

        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(totalInDb).isEqualTo(threadCount);
    }

    // === 2. 동시 답변 시작 (같은 문의에 10명 동시 시도) ===

    @Test
    @DisplayName("10명이 동시에 답변 시작 - 1명만 성공, 나머지 거절")
    void concurrent_startAnswering() throws InterruptedException {
        CustomUser user = userOf(member);
        QnaDetailResponse created = qnaService.createQuestion(user,
                new QnaCreateRequest(software.getId(), "답변 경합 테스트", "내용"));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    qnaService.startAnswering(adminUser(), created.getQnaId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        long elapsed = System.currentTimeMillis() - start;

        QnaQuestion result = qnaQuestionRepository.findById(created.getQnaId()).orElseThrow();
        System.out.println("[동시 답변시작] 성공: " + successCount + ", 실패: " + failCount
                + ", 최종 상태: " + result.getStatus() + ", 소요: " + elapsed + "ms");

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
        assertThat(result.getStatus()).isEqualTo(QnaStatus.ANSWERING);
    }

    // === 3. 등록과 삭제 동시 수행 ===

    @Test
    @DisplayName("등록 30건 + 삭제 동시 수행 - 데이터 정합성 유지")
    void concurrent_createAndDelete() throws InterruptedException {
        // 먼저 20건 등록
        List<Long> existingIds = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < 20; i++) {
            QnaDetailResponse q = qnaService.createQuestion(userOf(member),
                    new QnaCreateRequest(software.getId(), "기존 문의 " + i, "내용"));
            existingIds.add(q.getQnaId());
        }

        int createCount = 30;
        int deleteCount = existingIds.size();
        int totalThreads = createCount + deleteCount;
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch latch = new CountDownLatch(totalThreads);
        AtomicInteger createSuccess = new AtomicInteger();
        AtomicInteger deleteSuccess = new AtomicInteger();
        AtomicInteger deleteFail = new AtomicInteger();

        long start = System.currentTimeMillis();

        // 등록 스레드
        for (int i = 0; i < createCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    qnaService.createQuestion(userOf(member),
                            new QnaCreateRequest(software.getId(), "신규 문의 " + idx, "내용"));
                    createSuccess.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("[등록 실패] " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 삭제 스레드
        for (Long id : existingIds) {
            executor.submit(() -> {
                try {
                    qnaService.deleteQuestion(userOf(member), id);
                    deleteSuccess.incrementAndGet();
                } catch (Exception e) {
                    deleteFail.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        long elapsed = System.currentTimeMillis() - start;

        long finalCount = qnaQuestionRepository.count();
        long expectedCount = createSuccess.get() + (deleteCount - deleteSuccess.get());

        System.out.println("[등록+삭제] 등록 성공: " + createSuccess + ", 삭제 성공: " + deleteSuccess
                + ", 삭제 실패: " + deleteFail + ", DB 최종: " + finalCount + ", 소요: " + elapsed + "ms");

        assertThat(finalCount).isEqualTo(expectedCount);
    }

    // === 4. 동시 상태 변경 (같은 문의) ===

    @Test
    @DisplayName("같은 문의에 CLOSED/PENDING 동시 변경 - 낙관적 락으로 충돌 제어")
    void concurrent_statusChange() throws InterruptedException {
        QnaDetailResponse created = qnaService.createQuestion(userOf(member),
                new QnaCreateRequest(software.getId(), "상태 경합", "내용"));

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final QnaStatus status = (i % 2 == 0) ? QnaStatus.CLOSED : QnaStatus.PENDING;
            executor.submit(() -> {
                try {
                    qnaService.changeStatus(adminUser(), created.getQnaId(),
                            new QnaStatusUpdateRequest(status));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        long elapsed = System.currentTimeMillis() - start;

        QnaQuestion result = qnaQuestionRepository.findById(created.getQnaId()).orElseThrow();
        System.out.println("[동시 상태변경] 성공: " + successCount + ", 실패(락 충돌): " + failCount
                + ", 최종 상태: " + result.getStatus() + ", 소요: " + elapsed + "ms");

        // 낙관적 락으로 인해 일부만 성공, 데이터 정합성은 유지
        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
        assertThat(result.getStatus()).isIn(QnaStatus.CLOSED, QnaStatus.PENDING);
    }
}
