package bitc.full502.final_project_team1.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "report", schema = "java502_team1_final_db")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // 보고서 PK

    /** 어떤 배정 건물 조사에 대한 보고서인지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = true)
    private UserBuildingAssignmentEntity assignment;

    /** 어떤 조사 결과에 대한 보고서인지 */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_result_id", nullable = false)
    private SurveyResultEntity surveyResult;

    /** PDF 저장 경로 */
    @Column(name = "pdf_path", nullable = false)
    private String pdfPath;

    /** 승인자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", nullable = false)
    private UserAccountEntity approvedBy;

    /** 승인 일시 */
    @Column(name = "approved_at", nullable = false)
    private LocalDateTime approvedAt;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
