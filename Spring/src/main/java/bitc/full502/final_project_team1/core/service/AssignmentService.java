package bitc.full502.final_project_team1.core.service;

import java.util.List;
import java.util.Map;

public interface AssignmentService {

  /** 지역 키워드(예: "강동") 기준으로 건물을 모아 사용자들에게 라운드로빈으로 배정 */
  int assignRegionRoundRobin(String keyword);

  /** 특정 사용자에게 배정된 (buildingId, lotAddress) 목록 */
  List<Map<String, Object>> getAssignments(Integer userId);

  /** 지정 사용자에게 여러 건물을 일괄 배정(선택) */
  int assignToUser(Integer userId, List<Long> buildingIds);
}
