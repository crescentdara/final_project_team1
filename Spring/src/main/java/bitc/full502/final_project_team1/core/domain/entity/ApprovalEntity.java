// src/main/java/bitc/full502/final_project_team1/core/domain/entity/ApprovalEntity.java
package bitc.full502.final_project_team1.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval", schema = "java502_team1_final_db")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;                   // PK

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;  // 결재 시각 (대기중엔 null)

  @Column(name = "reject_reason", length = 500)
  private String rejectReason;

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
  private Long surveyResultId;

  // 조사원(Researcher)
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "surveyor_id")
  private UserAccountEntity surveyor;
}
