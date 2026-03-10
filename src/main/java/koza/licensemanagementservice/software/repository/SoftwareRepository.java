package koza.licensemanagementservice.software.repository;

import koza.licensemanagementservice.software.entity.Software;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoftwareRepository extends JpaRepository<Software, Long>, SoftwareRepositoryCustom {
    List<Software> findByMemberId(Long memberId);
}
