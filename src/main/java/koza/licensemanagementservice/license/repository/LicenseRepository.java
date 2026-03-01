package koza.licensemanagementservice.license.repository;

import koza.licensemanagementservice.license.entity.License;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long>, LicenseRepositoryCustom {
    Optional<License> findByLicenseKey(String licenseKey);
    Page<License> findBySoftwareId(Long softwareId, Pageable pageable);
    List<License> findByIdIn(List<Long> id);
    int countBySoftwareId(Long softwareId);
    boolean existsByLicenseKey(String licenseKey);
}
