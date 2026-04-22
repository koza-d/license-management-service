package koza.licensemanagementservice.domain.member.log.repository;

import koza.licensemanagementservice.domain.member.log.entity.MemberLog;
import koza.licensemanagementservice.domain.member.log.entity.MemberLogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberLogRepository extends MemberLogRepositoryCustom, JpaRepository<MemberLog, Long> {
    List<MemberLog> findByMemberIdOrderByCreateAtDesc(Long memberId);
    List<MemberLog> findByMemberIdAndLogTypeOrderByCreateAtDesc(Long memberId, MemberLogType logType);
}
