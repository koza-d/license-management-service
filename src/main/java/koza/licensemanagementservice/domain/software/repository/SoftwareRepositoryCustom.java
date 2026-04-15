package koza.licensemanagementservice.domain.software.repository;

import koza.licensemanagementservice.dashboard.dto.SoftwareStatsResponse;
import koza.licensemanagementservice.dashboard.dto.SoftwareDailyUsage;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareAdminDetailResponse;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareAdminStatsResponse;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareAdminSummaryResponse;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareSummaryResponse;
import koza.licensemanagementservice.domain.software.entity.Software;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SoftwareRepositoryCustom {
    SoftwareAdminDetailResponse findBySoftwareId(Long softwareId);
    Optional<Software> findByIdWithMember(Long softwareId);
    List<SoftwareDailyUsage> findDailyUsageByMemberId(Long memberId, LocalDateTime startDate);
    List<SoftwareStatsResponse> findSoftwareStatsByMemberId(Long memberId);
    Page<SoftwareSummaryResponse> findSummaryByMemberId(Long memberId, String search, boolean activeOnly, Pageable pageable);
    Page<SoftwareAdminSummaryResponse> searchSoftwareByCondition(SoftwareAdminSearchCondition condition, Pageable pageable);

    SoftwareAdminStatsResponse getSoftwareUsageStat(Long softwareId);
}
