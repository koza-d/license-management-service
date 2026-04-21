package koza.licensemanagementservice.domain.member.repository;

import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends MemberRepositoryCustom, JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    @Query("select m from Member m " +
            "where (:status is null or m.status = :status) " +
            "  and (:keyword is null or :keyword = '' " +
            "       or lower(m.email)    like lower(concat('%', :keyword, '%')) " +
            "       or lower(m.nickname) like lower(concat('%', :keyword, '%')))")
    Page<Member> searchForAdmin(@Param("keyword") String keyword,
                                @Param("status") MemberStatus status,
                                Pageable pageable);
}
