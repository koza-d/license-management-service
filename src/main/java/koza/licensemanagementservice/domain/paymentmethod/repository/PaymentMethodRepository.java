package koza.licensemanagementservice.domain.paymentmethod.repository;

import koza.licensemanagementservice.domain.paymentmethod.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    Optional<PaymentMethod> findByMemberIdAndCardCompanyAndCardNumberMaskedAndActiveTrue(Long memberId, String cardCompany, String cardNumberMasked);
    List<PaymentMethod> findByMemberId(Long memberId);

}
