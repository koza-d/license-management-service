package koza.licensemanagementservice.software.service;

import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.member.dto.CustomUser;
import koza.licensemanagementservice.member.repository.MemberRepository;
import koza.licensemanagementservice.software.dto.SoftwareVersionDTO;
import koza.licensemanagementservice.software.entity.Software;
import koza.licensemanagementservice.software.entity.SoftwareVersion;
import koza.licensemanagementservice.software.repository.SoftwareRepository;
import koza.licensemanagementservice.software.repository.SoftwareVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SoftwareVersionService {
    private final MemberRepository memberRepository;
    private final SoftwareRepository softwareRepository;
    private final SoftwareVersionRepository versionRepository;

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
}
