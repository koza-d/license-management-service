package koza.licensemanagementservice.domain.qna.repository;

import koza.licensemanagementservice.domain.qna.entity.QnaQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QnaQuestionRepository extends JpaRepository<QnaQuestion, Long>, QnaQuestionRepositoryCustom {

    @Query("SELECT q FROM QnaQuestion q JOIN FETCH q.software WHERE q.id = :id")
    Optional<QnaQuestion> findByIdWithSoftware(@Param("id") Long id);
}
