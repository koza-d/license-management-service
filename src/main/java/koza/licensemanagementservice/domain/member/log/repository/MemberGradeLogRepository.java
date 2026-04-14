package koza.licensemanagementservice.domain.member.log.repository;

import koza.licensemanagementservice.domain.member.log.entity.MemberGradeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberGradeLogRepository extends JpaRepository<MemberGradeLog, Long> {
    List<MemberGradeLog> findByMemberIdOrderByCreateAtDesc(Long memberId);
}
