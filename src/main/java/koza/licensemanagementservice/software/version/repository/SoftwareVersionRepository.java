package koza.licensemanagementservice.software.version.repository;

import koza.licensemanagementservice.software.version.entity.SoftwareVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SoftwareVersionRepository extends JpaRepository<SoftwareVersion, Long> {
    Optional<SoftwareVersion> findBySoftwareIdAndVersion(Long softwareId, String version);
    List<SoftwareVersion> findBySoftwareId(Long softwareId);
}
