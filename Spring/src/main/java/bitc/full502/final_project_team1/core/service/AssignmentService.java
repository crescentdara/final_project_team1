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

            UserBuildingAssignmentEntity a = UserBuildingAssignmentEntity.builder()
                    .buildingId(b.getId())
                    .user(user)
                    .assignedAt(LocalDateTime.now())
                    .build();

            assignRepo.save(a);
            created++;
        }
        return created;
    }

    // 유저별 배정 목록을 (buildingId, lotAddress) 맵으로 반환
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAssignments(Long userId) {
        List<Object[]> rows = assignRepo.findPairsByUserId(userId);
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Map<String, Object> m = new HashMap<>();
            m.put("buildingId", (Long) r[0]);
            m.put("lotAddress", (String) r[1]);
            out.add(m);
        }
        return out;
    }

    /** 조사 거절 **/
//    @Transactional
//    public void rejectAssignment(Long buildingId) {
//        // 1. 배정 삭제
//        assignRepo.deleteById(buildingId);
//
//        // 2. 건물 상태 = 미배정(0)
//        BuildingEntity building = buildingRepo.findById(buildingId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 건물이 존재하지 않습니다."));
//        building.setStatus(0);
//        buildingRepo.save(building);
//    }

    @Transactional
    public void rejectAssignment(Long userId, Long buildingId) {
        // 배정 존재/소유자 확인
        UserBuildingAssignmentEntity asg = assignRepo
                .findByBuildingIdAndUser_UserId(buildingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("배정 내역이 없습니다."));

        // 배정 삭제
        assignRepo.delete(asg);
        // 또는: assignRepo.deleteByBuildingIdAndUser_UserId(buildingId, userId);

        // 건물 상태 미배정(0)으로
        BuildingEntity b = buildingRepo.findById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 건물이 없습니다."));
        b.setStatus(0); // dirty checking
    }



