package koza.licensemanagementservice.dashboard.repository;

import koza.licensemanagementservice.dashboard.dto.response.AdminDashboardLicenseStatsResponse;
import koza.licensemanagementservice.dashboard.dto.response.AdminStatsResponse;
import org.springframework.stereotype.Component;

public interface DashboardRepository {
    AdminStatsResponse getAdminStats();
    AdminDashboardLicenseStatsResponse getAdminLicenseStats();
}
