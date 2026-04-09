package koza.licensemanagementservice.domain.faq.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.global.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "faq")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Faq extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "software_id", nullable = false)
    private Software software;

    @Column(name = "category", length = 30)
    private String category;

    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(name = "answer", columnDefinition = "TEXT", nullable = false)
    private String answer;

    @Column(name = "sort_order")
    @Builder.Default
    private int sortOrder = 0;

    public void update(String category, String question, String answer, Integer sortOrder) {
        if (category != null) this.category = category;
        if (question != null) this.question = question;
        if (answer != null) this.answer = answer;
        if (sortOrder != null) this.sortOrder = sortOrder;
    }
}
