package koza.licensemanagementservice.domain.faq.repository;

import koza.licensemanagementservice.domain.faq.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findBySoftwareIdOrderBySortOrderAsc(Long softwareId);
}
