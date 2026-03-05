package koza.licensemanagementservice.license.repository;


import koza.licensemanagementservice.license.entity.License;

import java.util.List;
import java.util.Optional;

public interface LicenseRepositoryCustom {
    Optional<License> findByIdWithSoftwareAndMember(Long licenseId);
    Optional<License> findByLicenseKeyWithSoftware(String licenseKey);
    List<License> findByIdInWithSoftwareWithMember(List<Long> ids);
}
