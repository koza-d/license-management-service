package koza.licensemanagementservice.domain.paymentmethod.repository;

import koza.licensemanagementservice.domain.paymentmethod.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
}
