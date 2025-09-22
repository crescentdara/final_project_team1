package bitc.full502.final_project_team1.api.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AssignApproverResponseDTO {

  private int assignedCount;         // 실제 변경(배정/재배정)된 건수
  private List<Long> updatedIds;     // 변경된 건물 id 목록 (선택)
}
