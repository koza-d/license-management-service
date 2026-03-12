package koza.licensemanagementservice.domain.software.repository;

import koza.licensemanagementservice.domain.software.entity.Software;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoftwareRepository extends JpaRepository<Software, Long>, SoftwareRepositoryCustom {
    List<Software> findByMemberId(Long memberId);
}
