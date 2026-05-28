package koza.licensemanagementservice.domain.plan.repository;

import koza.licensemanagementservice.domain.plan.entity.Plan;
import koza.licensemanagementservice.domain.plan.entity.PlanCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByPlanCode(PlanCode planCode);
}
