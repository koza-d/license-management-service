package koza.licensemanagementservice.license.repository;


import koza.licensemanagementservice.license.entity.License;

import java.util.List;
import java.util.Optional;

public interface LicenseRepositoryCustom {
    Optional<License> findByIdWithSoftwareAndMember(Long licenseId);
    List<License> findByIdInWithSoftwareWithMember(List<Long> ids);
}
