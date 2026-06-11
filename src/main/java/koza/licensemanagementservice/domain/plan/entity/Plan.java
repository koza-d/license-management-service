package koza.licensemanagementservice.domain.plan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plans")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Plan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_code", length = 20, nullable = false)
    private PlanCode planCode;

    @Column(name = "monthly_price")
    private int monthlyPrice;

    @Column(name = "yearly_price")
    private int yearlyPrice;

    @Column(name = "limit_license")
    private int limitLicense;
}
