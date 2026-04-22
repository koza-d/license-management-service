package koza.licensemanagementservice.domain.qna.repository;

import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.qna.dto.response.QnaListResponse;
import koza.licensemanagementservice.domain.qna.entity.QnaQuestion;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.global.config.QueryDslConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("tc")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QueryDslConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QnaQueryPerformanceTest {

    @Autowired
    private QnaQuestionRepository qnaQuestionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SoftwareRepository softwareRepository;

    private static final int TOTAL_QUESTIONS = 10_000;

    private Member member;
    private Software software1;
    private Software software2;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(
                Member.builder().email("test@test.com").password("pw").nickname("테스터").build()
        );
        software1 = softwareRepository.save(
                Software.builder().member(member).name("MyApp").latestVersion("1.0").apiKey("key1").limitLicense(100).build()
        );
        software2 = softwareRepository.save(
                Software.builder().member(member).name("OtherApp").latestVersion("2.0").apiKey("key2").limitLicense(50).build()
        );
    }

    private void bulkInsert(int count) {
        List<QnaQuestion> batch = new ArrayList<>(1000);
        for (int i = 0; i < count; i++) {
            Software sw = (i % 3 == 0) ? software2 : software1;
            QnaStatus status = switch (i % 3) {
                case 0 -> QnaStatus.PENDING;
                case 1 -> QnaStatus.ANSWERED;
                default -> QnaStatus.CLOSED;
            };

            QnaQuestion q = QnaQuestion.builder()
                    .software(sw)
                    .member(member)
                    .nickname("유저" + i)
                    .title("문의 제목 " + i)
                    .content("문의 내용입니다. 번호: " + i + " 라이센스 오류가 발생했습니다.")
                    .build();
            q.changeStatus(status);
            batch.add(q);

            if (batch.size() == 1000) {
                qnaQuestionRepository.saveAll(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            qnaQuestionRepository.saveAll(batch);
        }
        qnaQuestionRepository.flush();
    }

    // === 페이징 성능 ===

    @Test
    @Order(1)
    @DisplayName("1만건 페이징 조회 - 첫 페이지")
    void paging_firstPage() {
        bulkInsert(TOTAL_QUESTIONS);

        long start = System.currentTimeMillis();
        Page<QnaListResponse> result = qnaQuestionRepository.findAllQuestions(null, null, PageRequest.of(0, 20));
        long elapsed = System.currentTimeMillis() - start;

        assertThat(result.getContent()).hasSize(20);
        assertThat(result.getTotalElements()).isEqualTo(TOTAL_QUESTIONS);
        System.out.println("[페이징-첫페이지] " + elapsed + "ms (total: " + result.getTotalElements() + ")");
    }

    @Test
    @Order(2)
    @DisplayName("1만건 페이징 조회 - 마지막 페이지")
    void paging_lastPage() {
        bulkInsert(TOTAL_QUESTIONS);

        int lastPage = (TOTAL_QUESTIONS / 20) - 1;
        long start = System.currentTimeMillis();
        Page<QnaListResponse> result = qnaQuestionRepository.findAllQuestions(null, null, PageRequest.of(lastPage, 20));
        long elapsed = System.currentTimeMillis() - start;

        assertThat(result.getContent()).hasSize(20);
        System.out.println("[페이징-마지막페이지] " + elapsed + "ms (page: " + lastPage + ")");
    }

    // === 상태 필터 성능 ===

    @Test
    @Order(3)
    @DisplayName("1만건 상태 필터 (PENDING)")
    void filter_byStatus() {
        bulkInsert(TOTAL_QUESTIONS);

        long start = System.currentTimeMillis();
        Page<QnaListResponse> result = qnaQuestionRepository.findAllQuestions(null, QnaStatus.PENDING, PageRequest.of(0, 20));
        long elapsed = System.currentTimeMillis() - start;

        assertThat(result.getContent()).hasSize(20);
        System.out.println("[상태필터-PENDING] " + elapsed + "ms (filtered total: " + result.getTotalElements() + ")");
    }

    // === 키워드 검색 성능 ===

    @Test
    @Order(4)
    @DisplayName("1만건 키워드 검색 (제목)")
    void search_byTitle() {
        bulkInsert(TOTAL_QUESTIONS);

        long start = System.currentTimeMillis();
        Page<QnaListResponse> result = qnaQuestionRepository.findAllQuestions("제목 500", null, PageRequest.of(0, 20));
        long elapsed = System.currentTimeMillis() - start;

        assertThat(result.getContent()).isNotEmpty();
        System.out.println("[키워드검색-제목] " + elapsed + "ms (matched: " + result.getTotalElements() + ")");
    }

    @Test
    @Order(5)
    @DisplayName("1만건 키워드 검색 (내용 LIKE)")
    void search_byContent() {
        bulkInsert(TOTAL_QUESTIONS);

        long start = System.currentTimeMillis();
        Page<QnaListResponse> result = qnaQuestionRepository.findAllQuestions("라이센스 오류", null, PageRequest.of(0, 20));
        long elapsed = System.currentTimeMillis() - start;

        assertThat(result.getContent()).hasSize(20);
        System.out.println("[키워드검색-내용] " + elapsed + "ms (matched: " + result.getTotalElements() + ")");
    }

    // === 소프트웨어 필터 + 검색 복합 ===

    @Test
    @Order(6)
    @DisplayName("1만건 소프트웨어 필터 + 상태 + 검색 복합 조건")
    void combined_filter() {
        bulkInsert(TOTAL_QUESTIONS);

        long start = System.currentTimeMillis();
        Page<QnaListResponse> result = qnaQuestionRepository.findBySoftwareId(
                software1.getId(), "제목", QnaStatus.ANSWERED, PageRequest.of(0, 20));
        long elapsed = System.currentTimeMillis() - start;

        assertThat(result.getContent()).isNotEmpty();
        System.out.println("[복합필터] " + elapsed + "ms (matched: " + result.getTotalElements() + ")");
    }

    // === fetch join 단건 조회 ===

    @Test
    @Order(7)
    @DisplayName("fetch join 단건 조회 vs 일반 조회")
    void fetchJoin_vs_normal() {
        bulkInsert(1000);
        Long questionId = qnaQuestionRepository.findAll(PageRequest.of(0, 1)).getContent().get(0).getId();

        // fetch join
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            QnaQuestion q = qnaQuestionRepository.findByIdWithSoftware(questionId).orElseThrow();
            q.getSoftware().getName(); // 강제 접근
        }
        long fetchJoinTime = System.currentTimeMillis() - start1;

        // 일반 findById
        long start2 = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            QnaQuestion q = qnaQuestionRepository.findById(questionId).orElseThrow();
            q.getSoftware().getName(); // Lazy → 추가 쿼리 발생
        }
        long normalTime = System.currentTimeMillis() - start2;

        System.out.println("[fetch join x100] " + fetchJoinTime + "ms");
        System.out.println("[일반 조회 x100]  " + normalTime + "ms");
        System.out.println("[차이] fetch join이 " + (normalTime - fetchJoinTime) + "ms 빠름");
    }
}
