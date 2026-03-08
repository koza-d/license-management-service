package koza.licensemanagementservice.session.repository;

import koza.licensemanagementservice.session.entity.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {
    @Query(value = """
                SELECT 
                    DATE(s.verify_at) AS date,
                    SUM(TIMESTAMPDIFF(MINUTE, s.verify_at, s.release_at)) AS minutes
                FROM session_log s
                WHERE s.license_id = :licenseId
                  AND s.verify_at >= :startDate
                GROUP BY DATE(s.verify_at)
                ORDER BY DATE(s.verify_at) ASC
            """, nativeQuery = true)
    List<DailyUsageResponse> findDailyUsage(@Param("licenseId") Long licenseId, @Param("startDate") LocalDateTime startDate);

    interface DailyUsageResponse {
        LocalDate getDate();

        Long getMinutes();
    }
}
