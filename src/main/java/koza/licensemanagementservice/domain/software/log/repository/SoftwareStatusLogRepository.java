package koza.licensemanagementservice.domain.software.log.repository;

import koza.licensemanagementservice.domain.software.log.entity.SoftwareStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoftwareStatusLogRepository extends JpaRepository<SoftwareStatusLog, Long> {
    List<SoftwareStatusLog> findBySoftwareIdOrderByCreateAtDesc(Long softwareId);
}
