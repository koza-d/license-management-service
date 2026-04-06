package koza.licensemanagementservice.domain.faq.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.faq.dto.request.FaqCreateRequest;
import koza.licensemanagementservice.domain.faq.dto.response.FaqResponse;
import koza.licensemanagementservice.domain.faq.entity.Faq;
import koza.licensemanagementservice.domain.faq.repository.FaqRepository;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FaqServiceTest {

    @InjectMocks
    private FaqService faqService;

    @Mock
    private FaqRepository faqRepository;
    @Mock
    private SoftwareRepository softwareRepository;

    private Member createMember(Long id) {
        return Member.builder().id(id).email("dev@test.com").nickname("개발자").build();
    }

    private Software createSoftware(Long id, Member member) {
        return Software.builder().id(id).member(member).name("테스트SW").latestVersion("1.0.0").apiKey("key").limitLicense(100).build();
    }

    private CustomUser createCustomUser(Long id) {
        return new CustomUser("dev@test.com", id, "개발자", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Nested
    @DisplayName("FAQ 목록 조회")
    class GetFaqs {
        @Test
        @DisplayName("성공 - 소프트웨어별 FAQ 조회")
        void success() {
            // given
            Member member = createMember(1L);
            Software software = createSoftware(1L, member);
            Faq faq1 = Faq.builder().id(1L).software(software).question("Q1").answer("A1").sortOrder(0).build();
            Faq faq2 = Faq.builder().id(2L).software(software).question("Q2").answer("A2").sortOrder(1).build();

            given(faqRepository.findBySoftwareIdOrderBySortOrderAsc(1L)).willReturn(List.of(faq1, faq2));

            // when
            List<FaqResponse> result = faqService.getFaqsBySoftware(1L);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getQuestion()).isEqualTo("Q1");
            assertThat(result.get(1).getQuestion()).isEqualTo("Q2");
        }
    }

    @Nested
    @DisplayName("FAQ 등록")
    class CreateFaq {
        @Test
        @DisplayName("성공 - 소유자가 FAQ 등록")
        void success() {
            // given
            Member member = createMember(1L);
            Software software = createSoftware(1L, member);
            CustomUser user = createCustomUser(1L);
            FaqCreateRequest request = new FaqCreateRequest("설치", "설치방법?", "다운로드하세요", 0);

            given(softwareRepository.findById(1L)).willReturn(Optional.of(software));

            Faq savedFaq = Faq.builder()
                    .id(1L).software(software).category("설치")
                    .question("설치방법?").answer("다운로드하세요").sortOrder(0)
                    .build();
            given(faqRepository.save(any(Faq.class))).willReturn(savedFaq);

            // when
            FaqResponse response = faqService.createFaq(user, 1L, request);

            // then
            assertThat(response.getQuestion()).isEqualTo("설치방법?");
            assertThat(response.getAnswer()).isEqualTo("다운로드하세요");
            verify(faqRepository).save(any(Faq.class));
        }

        @Test
        @DisplayName("실패 - 소유자가 아닌 경우")
        void fail_notOwner() {
            // given
            Member owner = createMember(1L);
            Software software = createSoftware(1L, owner);
            CustomUser otherUser = createCustomUser(2L);
            FaqCreateRequest request = new FaqCreateRequest("설치", "질문", "답변", 0);

            given(softwareRepository.findById(1L)).willReturn(Optional.of(software));

            // when & then
            assertThatThrownBy(() -> faqService.createFaq(otherUser, 1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.ACCESS_DENIED);
        }
    }

    @Nested
    @DisplayName("FAQ 삭제")
    class DeleteFaq {
        @Test
        @DisplayName("성공 - 소유자가 FAQ 삭제")
        void success() {
            // given
            Member member = createMember(1L);
            Software software = createSoftware(1L, member);
            CustomUser user = createCustomUser(1L);
            Faq faq = Faq.builder().id(1L).software(software).question("Q").answer("A").build();

            given(faqRepository.findById(1L)).willReturn(Optional.of(faq));

            // when
            faqService.deleteFaq(user, 1L);

            // then
            verify(faqRepository).delete(faq);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 FAQ")
        void fail_notFound() {
            // given
            CustomUser user = createCustomUser(1L);
            given(faqRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> faqService.deleteFaq(user, 999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.FAQ_NOT_FOUND);
        }
    }
}
