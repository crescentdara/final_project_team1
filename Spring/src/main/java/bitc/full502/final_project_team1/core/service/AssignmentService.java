// src/main/java/bitc/full502/final_project_team1/core/service/AssignmentService.java
package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.AssignedBuildingDto;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;

import java.util.List;
import java.util.Map;

public interface AssignmentService {

  /**
   * 지역 키워드(예: "강동") 기준으로 건물을 모아 사용자들에게 라운드로빈으로 배정.
   * 기존 같은 지역 배정은 제거 후 재배정.
   * @return 생성(저장)된 배정 수
   */
  int assignRegionRoundRobin(String keyword);

  /**
   * 특정 사용자에게 배정된 (buildingId, lotAddress) 목록 조회.
   * @param userId Long (null이면 예외)
   */
  List<Map<String, Object>> getAssignments(Long userId);

  /**
   * 지정 사용자에게 여러 건물을 일괄 배정.
   * 구현에서는 다음을 수행해야 함:
   *  - user_building_assignment에 배정(중복 보호)
   *  - 필요 시 BuildingEntity.assignedUser/상태 갱신
   *  - 각 건물에 대해 ApprovalEntity 대기 레코드 생성(중복 보호)
   * @param userId Long (필수)
   * @param buildingIds 배정할 건물 ID 목록(비어있으면 0 리턴)
   * @return 처리(삽입/업데이트) 건수
   * @throws IllegalArgumentException userId가 없거나 존재하지 않으면
   */
  int assignToUser(Long userId, List<Long> buildingIds);

  /** 사용자 단건 조회(orThrow). 컨트롤러나 다른 서비스에서 재사용 가능. */
  UserAccountEntity getUserOrThrow(Long userId);

  /**
   * 결재자(ROLE=APPROVER) 검색.
   * 역할 컬럼이 없는 환경이라면 구현부에서 역할 필터를 생략하고 keyword만으로 검색.
   * @param keyword null/blank 허용(전체 조회)
   * @return 결재자 후보 목록
   */
  List<UserAccountEntity> searchApprovers(String keyword);

//  List<AssignedBuildingDto> findAssignedByRegion(String region);
}
