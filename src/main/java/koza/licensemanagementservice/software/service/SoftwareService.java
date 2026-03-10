package koza.licensemanagementservice.software.service;

import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.license.repository.LicenseRepository;
import koza.licensemanagementservice.member.dto.CustomUser;
import koza.licensemanagementservice.member.entity.Member;
import koza.licensemanagementservice.member.repository.MemberRepository;
import koza.licensemanagementservice.software.dto.SoftwareDTO;
import koza.licensemanagementservice.software.entity.Software;
import koza.licensemanagementservice.software.entity.SoftwareVersion;
import koza.licensemanagementservice.software.repository.SoftwareRepository;
import koza.licensemanagementservice.software.repository.SoftwareVersionRepository;
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
    public SoftwareDTO.CreateResponse createSoftware(CustomUser user, SoftwareDTO.CreateRequest createRequest) {
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
        return SoftwareDTO.CreateResponse.from(save);
    }

    @Transactional(readOnly = true)
    public SoftwareDTO.DetailResponse getSoftwareDetail(CustomUser user, Long softwareId) {
        Software software = softwareRepository.findByIdWithMember(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!software.getMember().getId().equals(user.getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        int licenseCount = licenseRepository.countBySoftwareId(softwareId);

        return SoftwareDTO.DetailResponse.of(software, licenseCount);
    }

    @Transactional(readOnly = true)
    public Page<SoftwareDTO.SummaryResponse> getSoftwareSummaryByMe(CustomUser user, String search, boolean activeOnly, Pageable pageable) {
        // 로그인한 유저가 보유한 소프트웨어 목록
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        return softwareRepository.findSummaryByMemberId(member.getId(), search, activeOnly,pageable);
    }

    @Transactional(readOnly = true)
    public List<SoftwareDTO.SimpleResponse> getSimpleList(CustomUser user) {
        // 로그인한 유저가 보유한 소프트웨어 목록
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        return softwareRepository.findByMemberId(user.getId())
                .stream().map(SoftwareDTO.SimpleResponse::of)
                .toList();
    }

    @Transactional
    public Long updateSoftware(CustomUser user, Long softwareId, SoftwareDTO.UpdateRequest updateRequest) {
        Software software = softwareRepository.findByIdWithMember(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!software.getMember().getId().equals(user.getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        software.updateInfo(updateRequest.getName(), updateRequest.getVersion());
        software.updateGlobalVariables(updateRequest.getGlobalVariables());
        software.updateLocalVariables(updateRequest.getLocalVariables());
        return softwareId;
    }
}
