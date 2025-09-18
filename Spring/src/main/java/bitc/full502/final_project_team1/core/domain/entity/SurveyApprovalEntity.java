package bitc.full502.final_project_team1.core.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "survey_approval")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyApprovalEntity {

    public enum Status { PENDING, APPROVED, REJECTED }
    public enum Priority { HIGH, MEDIUM, LOW}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                       // 결재 요청 PK

    @Column(length = 50, nullable = false, unique = true)
    private String caseNo;                 // 관리번호 (외부/안드로이드 식별자 매핑)

    @Column(length = 100, nullable = false)
    private String investigator;           // 조사원 명(또는 ID)

    @Column(length = 255, nullable = false)
    private String address;                // 주소 등 노출

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status;                 // PENDING/APPROVED/REJECTED

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Priority priority;             // HIGH/MEDIUM/LOW

    @Column(nullable = false)
    private LocalDateTime submittedAt;     // 접수일(안드로이드 업로드 시각)

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
