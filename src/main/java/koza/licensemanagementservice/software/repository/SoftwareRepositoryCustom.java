package koza.licensemanagementservice.software.repository;

import koza.licensemanagementservice.dashboard.dto.SoftwareStatsResponse;
import koza.licensemanagementservice.dashboard.dto.SoftwareDailyUsage;
import koza.licensemanagementservice.software.dto.SoftwareDTO;
import koza.licensemanagementservice.software.dto.response.SoftwareSummaryResponse;
import koza.licensemanagementservice.software.entity.Software;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SoftwareRepositoryCustom {
    Optional<Software> findByIdWithMember(Long softwareId);
    List<SoftwareDailyUsage> findDailyUsageByMemberId(Long memberId, LocalDateTime startDate);
    List<SoftwareStatsResponse> findSoftwareStatsByMemberId(Long memberId);
    Page<SoftwareSummaryResponse> findSummaryByMemberId(Long memberId, String search, boolean activeOnly, Pageable pageable);
}
