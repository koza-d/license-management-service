package koza.licensemanagementservice.domain.session.log.repository;

import koza.licensemanagementservice.domain.session.log.entity.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionLogRepository extends JpaRepository<SessionLog, Long>, SessionLogRepositoryCustom {
    @Query(value = """
            SELECT
                DAYOFWEEK(ts) - 1 AS unit,
                MAX(concurrent) AS max,
                ROUND(AVG(concurrent), 2) AS avg
            FROM (
                SELECT
                  ts,
                  SUM(delta) OVER (ORDER BY ts) AS concurrent
                FROM (
                  SELECT verify_at AS ts, +1 AS delta FROM lms.session_log
                  UNION ALL
                  SELECT release_at AS ts, -1 AS delta FROM lms.session_log
                ) AS raw_data
                WHERE ts BETWEEN :from AND :to
            ) AS day_data
            GROUP BY unit
            ORDER BY CAST(unit AS UNSIGNED)
                """, nativeQuery = true)
    List<SessionPeakInterface> findPeakByDays(LocalDateTime from, LocalDateTime to);

    @Query(value = """
            SELECT
                DATE_FORMAT(ts, '%H') AS unit,
                MAX(concurrent) AS max,
                ROUND(AVG(concurrent), 2) AS avg
            FROM (
                SELECT
                  ts,
                  SUM(delta) OVER (ORDER BY ts) AS concurrent
                FROM (
                  SELECT verify_at AS ts, +1 AS delta FROM lms.session_log
                  UNION ALL
                  SELECT release_at AS ts, -1 AS delta FROM lms.session_log
                ) AS raw_data
                WHERE ts BETWEEN :from AND :to
            ) AS day_data
            GROUP BY unit
            ORDER BY CAST(unit AS UNSIGNED)
                """, nativeQuery = true)
    List<SessionPeakInterface> findPeakByHours(LocalDateTime from, LocalDateTime to);

    interface SessionPeakInterface {
        Integer getUnit();
        Double getAvg();
        Integer getMax();
    }
}
