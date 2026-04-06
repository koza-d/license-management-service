package koza.licensemanagementservice.domain.faq.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.faq.dto.request.FaqCreateRequest;
import koza.licensemanagementservice.domain.faq.dto.request.FaqUpdateRequest;
import koza.licensemanagementservice.domain.faq.dto.response.FaqResponse;
import koza.licensemanagementservice.domain.faq.entity.Faq;
import koza.licensemanagementservice.domain.faq.repository.FaqRepository;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FaqService {
    private final FaqRepository faqRepository;
    private final SoftwareRepository softwareRepository;

    @Transactional(readOnly = true)
    public List<FaqResponse> getFaqsBySoftware(Long softwareId) {
        return faqRepository.findBySoftwareIdOrderBySortOrderAsc(softwareId)
                .stream()
                .map(FaqResponse::from)
                .toList();
    }

    @Transactional
    public FaqResponse createFaq(CustomUser user, Long softwareId, FaqCreateRequest request) {
        Software software = getSoftwareWithOwnerCheck(user.getId(), softwareId);

        Faq faq = Faq.builder()
                .software(software)
                .category(request.getCategory())
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        Faq saved = faqRepository.save(faq);
        return FaqResponse.from(saved);
    }

    @Transactional
    public FaqResponse updateFaq(CustomUser user, Long faqId, FaqUpdateRequest request) {
        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FAQ_NOT_FOUND));

        validateSoftwareOwner(user.getId(), faq.getSoftware());

        faq.update(request.getCategory(), request.getQuestion(), request.getAnswer(), request.getSortOrder());
        return FaqResponse.from(faq);
    }

    @Transactional
    public void deleteFaq(CustomUser user, Long faqId) {
        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FAQ_NOT_FOUND));

        validateSoftwareOwner(user.getId(), faq.getSoftware());

        faqRepository.delete(faq);
    }

    private Software getSoftwareWithOwnerCheck(Long memberId, Long softwareId) {
        Software software = softwareRepository.findById(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        validateSoftwareOwner(memberId, software);
        return software;
    }

    private void validateSoftwareOwner(Long memberId, Software software) {
        if (!software.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}
