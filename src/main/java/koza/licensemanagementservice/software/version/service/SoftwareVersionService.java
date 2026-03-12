package koza.licensemanagementservice.software.version.service;

import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.member.entity.Member;
import koza.licensemanagementservice.member.repository.MemberRepository;
import koza.licensemanagementservice.software.version.dto.*;
import koza.licensemanagementservice.software.entity.Software;
import koza.licensemanagementservice.software.version.entity.SoftwareVersion;
import koza.licensemanagementservice.software.repository.SoftwareRepository;
import koza.licensemanagementservice.software.version.repository.SoftwareVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SoftwareVersionService {
    private final MemberRepository memberRepository;
    private final SoftwareRepository softwareRepository;
    private final SoftwareVersionRepository versionRepository;

    @Transactional
    public void createVersion(CustomUser user, SoftwareVersionCreateRequest request) {
        Software software = getSoftwareOrThrow(request.getSoftwareId(), user.getId());

        versionRepository.findBySoftwareIdAndVersion(request.getSoftwareId(), request.getVersion())
                .ifPresent(version -> { throw new BusinessException(ErrorCode.DUPLICATE_VERSION); });

        SoftwareVersion version = SoftwareVersion.builder()
                .software(software)
                .version(request.getVersion())
                .fileHash(request.getFileHash())
                .isAvailable(request.isAvailable())
                .downloadURL(request.getDownloadURL())
                .memo(request.getMemo())
                .build();

        versionRepository.save(version);
    }

    @Transactional(readOnly = true)
    public SoftwareVersionDetailResponse getVersion(CustomUser user, Long versionId) {
        SoftwareVersion version = versionRepository.findById(versionId) // 추후 WithSoftware 로 변경
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!user.getId().equals(version.getSoftware().getMember().getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        return SoftwareVersionDetailResponse.from(version);
    }

    @Transactional(readOnly = true)
    public List<SoftwareVersionSummaryResponse> getVersions(CustomUser user, Long softwareId) {
        getSoftwareOrThrow(softwareId, user.getId());

        return versionRepository.findBySoftwareId(softwareId).stream()
                .map(SoftwareVersionSummaryResponse::from)
                .sorted(Comparator.reverseOrder()).toList();

    }

    @Transactional
    public void updateVersion(CustomUser user, Long versionId, SoftwareVersionUpdateRequest request) {
        SoftwareVersion version = versionRepository.findById(versionId) // 추후 WithSoftware 로 변경
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Software software = version.getSoftware();
        if (!version.getVersion().equals(request.getVersion())) { // 버전을 변경할 경우에 버전 중복체크
            versionRepository.findBySoftwareIdAndVersion(software.getId(), request.getVersion())
                    .ifPresent(v -> {
                        throw new BusinessException(ErrorCode.DUPLICATE_VERSION);
                    });
        }
        Member owner = software.getMember();
        if (!user.getId().equals(owner.getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        version.updateVersionInfo(request);
    }

    private Software getSoftwareOrThrow(Long softwareId, Long memberId) {
        Software software = softwareRepository.findByIdWithMember(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!software.getMember().getId().equals(memberId))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        return software;
    }
}
