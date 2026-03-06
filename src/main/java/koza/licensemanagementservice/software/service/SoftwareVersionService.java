package koza.licensemanagementservice.software.service;

import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.member.dto.CustomUser;
import koza.licensemanagementservice.member.entity.Member;
import koza.licensemanagementservice.member.repository.MemberRepository;
import koza.licensemanagementservice.software.dto.SoftwareVersionDTO;
import koza.licensemanagementservice.software.entity.Software;
import koza.licensemanagementservice.software.entity.SoftwareVersion;
import koza.licensemanagementservice.software.repository.SoftwareRepository;
import koza.licensemanagementservice.software.repository.SoftwareVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SoftwareVersionService {
    private final MemberRepository memberRepository;
    private final SoftwareRepository softwareRepository;
    private final SoftwareVersionRepository versionRepository;

    @Transactional
    public void createVersion(CustomUser user, SoftwareVersionDTO.CreateRequest request) {
        Software software = softwareRepository.findByIdWithMember(request.getSoftwareId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!user.getId().equals(software.getMember().getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

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
    public SoftwareVersionDTO.DetailResponse getVersion(CustomUser user, Long versionId) {
        SoftwareVersion version = versionRepository.findById(versionId) // 추후 WithSoftware 로 변경
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Software software = version.getSoftware();
        Member owner = software.getMember();
        if (!user.getId().equals(owner.getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        return SoftwareVersionDTO.DetailResponse.from(version);
    }

    @Transactional
    public void updateVersion(CustomUser user, Long versionId, SoftwareVersionDTO.UpdateRequest request) {
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
}
