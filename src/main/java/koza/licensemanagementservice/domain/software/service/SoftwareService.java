package koza.licensemanagementservice.domain.software.service;

import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.software.dto.request.SoftwareCreateRequest;
import koza.licensemanagementservice.domain.software.dto.request.SoftwareUpdateRequest;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareDetailResponse;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareSimpleResponse;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.domain.software.version.entity.SoftwareVersion;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareCreateResponse;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareSummaryResponse;
import koza.licensemanagementservice.domain.software.entity.Software;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SoftwareService {
    private final SoftwareRepository softwareRepository;
    private final LicenseRepository licenseRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public SoftwareCreateResponse createSoftware(CustomUser user, SoftwareCreateRequest createRequest) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        String apiKey = SoftwareKeyGenerator.generateApiKey();
        int limitLicense = 100; // 임시 값, 추후 수정

        Software software = Software.builder()
                .name(createRequest.getName())
                .latestVersion(createRequest.getLatestVersion())
                .member(member)
                .apiKey(apiKey)
                .globalVariables(createRequest.getGlobalVariables())
                .localVariables(createRequest.getLocalVariables())
                .limitLicense(limitLicense)
                .build();

        SoftwareVersion version = SoftwareVersion.builder()
                .version(createRequest.getLatestVersion())
                .isAvailable(true)
                .build();

        software.addVersion(version);
        Software save = softwareRepository.save(software);
        return SoftwareCreateResponse.from(save);
    }

    @Transactional(readOnly = true)
    public SoftwareDetailResponse getSoftwareDetail(CustomUser user, Long softwareId) {
        Software software = getSoftwareOrElse(user.getId(), softwareId);

        int licenseCount = licenseRepository.countBySoftwareId(softwareId);

        return SoftwareDetailResponse.of(software, licenseCount);
    }

    @Transactional(readOnly = true)
    public Page<SoftwareSummaryResponse> getSoftwareSummaryByMe(CustomUser user, String search, boolean activeOnly, Pageable pageable) {
        // 로그인한 유저가 보유한 소프트웨어 목록
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        return softwareRepository.findSummaryByMemberId(member.getId(), search, activeOnly,pageable);
    }

    @Transactional(readOnly = true)
    public List<SoftwareSimpleResponse> getSimpleList(CustomUser user) {
        // 로그인한 유저가 보유한 소프트웨어 목록
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        return softwareRepository.findByMemberId(user.getId())
                .stream().map(SoftwareSimpleResponse::of)
                .toList();
    }

    @Transactional
    public Long updateSoftware(CustomUser user, Long softwareId, SoftwareUpdateRequest updateRequest) {
        Software software = getSoftwareOrElse(user.getId(), softwareId);

        software.updateInfo(updateRequest.getName(), updateRequest.getVersion());
        software.updateGlobalVariables(updateRequest.getGlobalVariables());
        software.updateLocalVariables(updateRequest.getLocalVariables());
        return softwareId;
    }

    private Software getSoftwareOrElse(Long memberId, Long softwareId) {
        Software software = softwareRepository.findByIdWithMember(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!software.getMember().getId().equals(memberId))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        return software;
    }
}
