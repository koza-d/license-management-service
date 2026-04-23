package koza.licensemanagementservice.domain.qna.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.global.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "qna")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Qna extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "software_id", nullable = false)
    private Software software;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;


    @Column(name = "nickname", length = 30, nullable = false)
    private String nickname;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private QnaStatus status = QnaStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20, nullable = false)
    @Builder.Default
    private QnaPriority priority = QnaPriority.NORMAL;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Version
    private Long version;

    public void update(Software software, String title, String content) {
        this.software = software;
        this.title = title;
        this.content = content;
    }

    public void submitAnswer(String answer) {
        this.answer = answer;
        this.answeredAt = LocalDateTime.now();
        this.status = QnaStatus.CLOSED;
    }

    public void updateAnswer(String answer) {
        this.answer = answer;
    }

    public void changeStatus(QnaStatus status) {
        this.status = status;
    }

    public void changePriority(QnaPriority priority) {
        this.priority = priority;
    }
}
