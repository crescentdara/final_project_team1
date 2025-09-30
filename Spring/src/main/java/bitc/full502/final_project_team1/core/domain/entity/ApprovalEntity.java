package bitc.full502.final_project_team1.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "approval",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_approval_building_surveyor", columnNames = {"building_id", "surveyor_id"})
        },
        indexes = {
                @Index(name = "idx_approval_approved_at", columnList = "approved_at")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 결재자(승인/반려 수행자) - 생성 시 null 가능 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id",
            foreignKey = @ForeignKey(name = "fk_approval_approver_user"))
    private UserAccountEntity approver;           // 결재자**

    /** 반려 사유 */
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;                  // 반려사유

    /** 승인 일시 (대기 중 null) */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;             // 일시

    /** 대상 빌딩 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_approval_building"))
    private BuildingEntity building;              // 빌딩id**

    /** 조사원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "surveyor_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_approval_surveyor_user"))
    private UserAccountEntity surveyor;           // 조사원id**

    /** 결재 대상 '조사결과' */
    @ManyToOne(fetch = FetchType.LAZY, optional = true) // null 허용
    @JoinColumn(name = "survey_result_id", nullable = true,
            foreignKey = @ForeignKey(name = "fk_approval_survey_result"))
    private SurveyResultEntity surveyResult;


//    @Column(name = "survey_result_id")
//    private Long surveyResultId;
}
