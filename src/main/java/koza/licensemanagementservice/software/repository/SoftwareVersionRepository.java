package koza.licensemanagementservice.software.repository;

import koza.licensemanagementservice.software.entity.SoftwareVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SoftwareVersionRepository extends JpaRepository<SoftwareVersion, Long> {
    Optional<SoftwareVersion> findBySoftwareIdAndVersion(Long softwareId, String version);
}
