package koza.licensemanagementservice.domain.payment.repository;

import koza.licensemanagementservice.domain.payment.entity.Payment;
import koza.licensemanagementservice.domain.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentRepositoryCustom {

    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.subscription " +
            "WHERE p.orderId = :orderId")
    Optional<Payment> findByOrderIdWithSubscription(@Param("orderId") String orderId);
    Optional<Payment> findByMemberIdAndStatus(Long memberId, PaymentStatus status);
    Optional<Payment> findBySubscriptionIdAndStatus(Long subscriptionId, PaymentStatus status);

    Page<Payment> findByMemberId(Long memberId, Pageable pageable);

    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.subscription s " +
            "LEFT JOIN FETCH p.member m " +
            "LEFT JOIN FETCH s.plan " +
            "WHERE p.id = :id")
    Optional<Payment> findByIdFetchAll(@Param("id") Long id);

    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.member " +
            "LEFT JOIN FETCH p.subscription s " +
            "LEFT JOIN FETCH s.plan " +
            "WHERE p.subscription.id = :subscriptionId " +
            "ORDER BY p.createAt DESC")
    List<Payment> findBySubscriptionIdFetchAll(@Param("subscriptionId") Long subscriptionId);
}
