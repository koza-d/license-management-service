package koza.licensemanagementservice.domain.subscription.repository;

import koza.licensemanagementservice.domain.subscription.entity.Subscription;
import koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, SubscriptionRepositoryCustom {
    @Query("SELECT s FROM Subscription s " +
            "JOIN FETCH s.member " +
            "JOIN FETCH s.plan " +
            "LEFT JOIN FETCH s.paymentMethod " +
            "WHERE s.id = :subscriptionId")
    Optional<Subscription> findByIdFetchAll(@Param("subscriptionId") Long subscriptionId);

    @Query("SELECT s FROM Subscription s " +
            "WHERE s.member.id = :memberId " +
            "AND s.status IN (koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus.ACTIVE, koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus.PAST_DUE, koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus.CANCELLED)")
    Optional<Subscription> findActiveSubscription(@Param("memberId") Long memberId);

    @Query("SELECT s FROM Subscription s " +
            "JOIN FETCH s.member " +
            "JOIN FETCH s.plan " +
            "JOIN FETCH s.paymentMethod " +
            "WHERE s.status IN (koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus.ACTIVE, koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus.PAST_DUE) " +
            "AND s.currentPeriodEnd <= :today " +
            "AND (s.gracePeriodEnd IS NULL OR s.gracePeriodEnd >= :today)")
    List<Subscription> findNeedRenewalSubscriptions(@Param("today") LocalDateTime todayStartAt);

    @Query("SELECT s FROM Subscription s " +
            "WHERE " +
            "(s.currentPeriodEnd < :today AND s.status = koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus.CANCELLED) " +
            "OR (s.gracePeriodEnd IS NOT NULL AND s.gracePeriodEnd < :today AND s.status = koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus.PAST_DUE)")
    List<Subscription> findExpiredTarget(@Param("today") LocalDateTime todayStartAt);

    Optional<Subscription> findByMemberIdAndStatus(Long memberId, SubscriptionStatus status);
}
