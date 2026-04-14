package koza.licensemanagementservice.domain.member.log.repository;

import koza.licensemanagementservice.domain.member.log.entity.MemberStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberStatusLogRepository extends JpaRepository<MemberStatusLog, Long> {
    List<MemberStatusLog> findByMemberIdOrderByCreateAtDesc(Long memberId);
}
