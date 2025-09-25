
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


    /** 반려 사유 */
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;                  // 반려사유

    /** 승인 일시 (대기 중 null) */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;             // 일시

  // 결재자
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "approver_id")
  private UserAccountEntity approver;

  // 조사 대상 건물
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "building_id")
  private BuildingEntity building;

  // (선택) 설문 결과 – 아직 없을 수 있음
  @Column(name = "survey_result_id")
  private Long surveyResult;

  // 조사원(Researcher)
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "surveyor_id")
  private UserAccountEntity surveyor;
}
