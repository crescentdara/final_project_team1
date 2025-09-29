package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.AssignedBuildingDto;
import bitc.full502.final_project_team1.api.web.dto.AssignApproverRequestDTO;
import bitc.full502.final_project_team1.api.web.dto.AssignApproverResponseDTO;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;

import java.util.List;
import java.util.Map;

public interface AssignmentService {


    /** 특정 사용자에게 배정된 (buildingId, lotAddress) 목록 조회 */
    List<Map<String, Object>> getAssignments(Long userId);

    /**
     * 지정 사용자에게 여러 건물을 일괄 "조사원" 배정
     * - user_building_assignment upsert
     * - Building.assignedUser/상태 갱신
     * - (정책에 따라) Approval 대기 레코드 생성은 구현에 따라 선택
     */
    int assignToUser(Long userId, List<Long> buildingIds);

    /** 사용자 단건 조회(orThrow) */
    UserAccountEntity getUserOrThrow(Long userId);

    /** 결재자(ROLE=APPROVER) 검색 (역할컬럼 없으면 keyword만으로 검색) */
    List<UserAccountEntity> searchApprovers(String keyword);

    /** ✅ 체크된 건물들에 결재자 배정(approval_id 세팅, status=결재대기) */
    AssignApproverResponseDTO assignApprover(AssignApproverRequestDTO req);

    void rejectAssignment(Long userId, Long buildingId);

}





//package bitc.full502.final_project_team1.core.service;

//
//import bitc.full502.final_project_team1.api.app.dto.AssignedBuildingDto;
//import bitc.full502.final_project_team1.api.web.dto.AssignApproverRequestDTO;
//import bitc.full502.final_project_team1.api.web.dto.AssignApproverResponseDTO;
//import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
//
//import java.util.List;
//import java.util.Map;
//
//public interface AssignmentService {
//
//
//    /** 특정 사용자에게 배정된 (buildingId, lotAddress) 목록 조회 */
//    List<Map<String, Object>> getAssignments(Long userId);
//
//    /**
//     * 지정 사용자에게 여러 건물을 일괄 "조사원" 배정
//     * - user_building_assignment upsert
//     * - Building.assignedUser/상태 갱신
//     * - (정책에 따라) Approval 대기 레코드 생성은 구현에 따라 선택
//     */
//    int assignToUser(Long userId, List<Long> buildingIds);
//
//    /** 사용자 단건 조회(orThrow) */
//    UserAccountEntity getUserOrThrow(Long userId);
//
//    /** 결재자(ROLE=APPROVER) 검색 (역할컬럼 없으면 keyword만으로 검색) */
//    List<UserAccountEntity> searchApprovers(String keyword);
//
//    /** ✅ 체크된 건물들에 결재자 배정(approval_id 세팅, status=결재대기) */
//    AssignApproverResponseDTO assignApprover(AssignApproverRequestDTO req);
//
//            UserBuildingAssignmentEntity a = UserBuildingAssignmentEntity.builder()
//                    .buildingId(b.getId())
//                    .user(user)
//                    .assignedAt(LocalDateTime.now())
//                    .build();
//
//            assignRepo.save(a);
//            created++;
//        }
//        return created;
//    }
//
//
//
//    /** 조사 거절 **/
////    @Transactional
////    public void rejectAssignment(Long buildingId) {
////        // 1. 배정 삭제
////        assignRepo.deleteById(buildingId);
////
////        // 2. 건물 상태 = 미배정(0)
////        BuildingEntity building = buildingRepo.findById(buildingId)
////                .orElseThrow(() -> new IllegalArgumentException("해당 건물이 존재하지 않습니다."));
////        building.setStatus(0);
////        buildingRepo.save(building);
////    }
//
//
//}
//
