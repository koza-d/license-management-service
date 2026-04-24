package koza.licensemanagementservice.domain.software.repository;

import koza.licensemanagementservice.domain.software.entity.Software;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SoftwareRepository extends JpaRepository<Software, Long>, SoftwareRepositoryCustom {
    Optional<Software> findByAppId(String appId);
    List<Software> findByMemberId(Long memberId);
    List<Software> findByStatusUntilBefore(LocalDateTime before);
}
