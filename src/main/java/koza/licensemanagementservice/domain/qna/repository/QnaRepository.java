package koza.licensemanagementservice.domain.qna.repository;

import koza.licensemanagementservice.domain.qna.entity.Qna;
import koza.licensemanagementservice.domain.qna.entity.QnaPriority;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QnaRepository extends JpaRepository<Qna, Long>, QnaRepositoryCustom {

    @Query("SELECT q FROM Qna q JOIN FETCH q.software WHERE q.id = :id")
    Optional<Qna> findByIdWithSoftware(@Param("id") Long id);

    Long countByStatus(QnaStatus status);
    Long countByStatusAndPriority(QnaStatus status, QnaPriority priority);
}
