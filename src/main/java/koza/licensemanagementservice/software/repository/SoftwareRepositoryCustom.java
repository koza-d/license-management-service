package koza.licensemanagementservice.software.repository;

import koza.licensemanagementservice.software.dto.SoftwareDTO;
import koza.licensemanagementservice.software.entity.Software;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface SoftwareRepositoryCustom {
    Optional<Software> findByIdWithMember(Long softwareId);
    Page<SoftwareDTO.SummaryResponse> findSummaryByMemberId(Long memberId, String search, Pageable pageable);
}
