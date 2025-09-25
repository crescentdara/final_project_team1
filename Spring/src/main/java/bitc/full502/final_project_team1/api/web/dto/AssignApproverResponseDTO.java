package bitc.full502.final_project_team1.api.web.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignApproverResponseDTO {

  private boolean success;
  /** 실제로 결재자가 배정된 개수 */
  private int assignedCount;

  /** 결과 상세(선택적으로 프론트에서 안내에 활용) */
  private List<Long> updatedIds;       // 이번에 성공적으로 배정된 building ids
  private List<Long> alreadyAssigned;  // 이미 approval_id가 있었던 building ids
  private List<Long> noResearcher;     // 조사원(user_id) 미배정으로 스킵된 building ids
  private List<Long> notFound;         // UBA 행이 없거나 building id 불일치
}
