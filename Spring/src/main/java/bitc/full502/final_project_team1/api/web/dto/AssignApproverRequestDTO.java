package bitc.full502.final_project_team1.api.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignApproverRequestDTO {

  private Long userId;           // 선택된 결재자 PK
  private List<Long> buildingIds;    // 선택된 건물 PK 목록
}
