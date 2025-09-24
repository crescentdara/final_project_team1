// src/main/java/.../core/service/AssignmentServiceImpl.java
package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.AssignedBuildingDto;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
import bitc.full502.final_project_team1.core.domain.repository.ApprovalRepository;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserBuildingAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService {

    private final BuildingRepository buildingRepo;
    private final UserAccountRepository userRepo;
    private final UserBuildingAssignmentRepository assignRepo;
    private final ApprovalRepository ApprovalRepo;

    private static String normOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** 지역 단위 라운드로빈 배정(데모/툴용) */
    @Override
    @Transactional
    public int assignRegionRoundRobin(String keyword) {
        // 기존 같은 지역 배정 제거(Repository에 메서드가 있어야 함)
        assignRepo.deleteAllByLotAddressLike(keyword);

        List<BuildingEntity> buildings = buildingRepo.findByLotAddressLike(keyword);
        List<UserAccountEntity> users = userRepo.findAll(Sort.by(Sort.Direction.ASC, "userId"));

        if (users.isEmpty() || buildings.isEmpty()) return 0;

        int u = users.size();
        int created = 0;
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < buildings.size(); i++) {
            BuildingEntity b = buildings.get(i);
            UserAccountEntity user = users.get(i % u);

            // PK=building_id라 중복 생성 불가 → 없으면 insert, 있으면 update가 필요
            Optional<UserBuildingAssignmentEntity> old = assignRepo.findById(b.getId());
            if (old.isPresent()) {
                UserBuildingAssignmentEntity a = old.get();
                a.setUser(user);
                a.setAssignedAt(now);
                a.setStatus(1); // 1=배정
                assignRepo.save(a);
            } else {
                UserBuildingAssignmentEntity a = UserBuildingAssignmentEntity.builder()
                    .buildingId(b.getId())
                    .user(user)
                    .assignedAt(now)
                    .status(1) // ★ not-null 보장
                    .build();
                assignRepo.save(a);
            }
            created++;
        }

        // (선택) Building 쪽에도 동기화하고 싶으면 한 방에 처리
        // buildingRepo.bulkAssign(user, buildings.stream().map(BuildingEntity::getId).toList());

        return created;
    }

    /** 특정 사용자에게 배정된 (buildingId, lotAddress) 목록 */
    @Override
    public List<Map<String, Object>> getAssignments(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId는 필수입니다.");
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

    /** 선택 건물들을 지정 사용자에게 일괄 배정 */
    @Override
    @Transactional
    public int assignToUser(Long userId, List<Long> buildingIds) {
        if (userId == null) throw new IllegalArgumentException("userId는 필수입니다.");
        if (buildingIds == null || buildingIds.isEmpty()) return 0;

        UserAccountEntity user = getUserOrThrow(userId);

        int affected = 0;
        LocalDateTime now = LocalDateTime.now();

        // 1) user_building_assignment 테이블 Upsert 느낌으로 처리
        for (Long bid : buildingIds) {
            Optional<UserBuildingAssignmentEntity> old = assignRepo.findById(bid);
            if (old.isPresent()) {
                UserBuildingAssignmentEntity a = old.get();
                // 기존 배정의 사용자/상태만 변경
                a.setUser(user);
                a.setAssignedAt(now);
                a.setStatus(1); // 1=배정
                assignRepo.save(a);
            } else {
                UserBuildingAssignmentEntity a = UserBuildingAssignmentEntity.builder()
                    .buildingId(bid)
                    .user(user)
                    .assignedAt(now)
                    .status(1) // ★ not-null 보장
                    .build();
                assignRepo.save(a);
            }
            affected++;
        }

        // 2) (권장) Building 테이블에도 배정자/상태를 동기화
        //    - Repository에 이미 아래 메서드가 있는 것으로 맞춰서 사용합니다.
        //    - 내부 JPQL:
        //      update BuildingEntity b set b.assignedUser = :user where b.id in :ids
        //    - Building.status 값을 쓰는 경우 함께 올리려면 쿼리를 확장하세요.
        buildingRepo.bulkAssign(user, buildingIds);

        // ※ ApprovalEntity(결재요청)는 '조사결과가 생성된 후' 만들어지는 것이 자연스럽습니다.
        //    (ApprovalEntity.surveyResult 가 nullable=false인 구조라,
        //     지금 시점(배정 시점)에는 생성할 수 없고, 조사결과 업로드/제출 시 만들어야 일관성이 맞습니다.)

        return affected;
    }

    @Override
    public UserAccountEntity getUserOrThrow(Long userId) {
        return userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자: " + userId));
    }

    /** 결재자(ROLE=APPROVER) 검색 (역할 컬럼 없으면 키워드만으로 검색 폴백) */
    @Override
    public List<UserAccountEntity> searchApprovers(String keyword) {
        String kw = normOrNull(keyword);
        // 프로젝트에 구현된 Repository 메서드에 맞춰 호출
        // - 역할 컬럼이 있으면 "APPROVER"로 필터
        // - 역할 컬럼이 없다면 Repository 구현을 키워드 전용으로 두고 여기서 그대로 위임
        try {
            return userRepo.searchApprovers("APPROVER", kw);
        } catch (Exception ignore) {
            // 역할 파라미터가 필요 없는 메서드가 있는 경우 폴백
            return userRepo.searchApprovers(null, kw);
        }
    }

//    /** ✅ 배정(ASSIGNED)만 region 조건으로 조회 */
//    @Override
//    public List<AssignedBuildingDto> findAssignedByRegion(String region) {
//        return ApprovalRepo.findAssignedByRegion(normOrNull(region));
//    }
}
