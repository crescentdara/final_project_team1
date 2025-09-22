// src/main/java/bitc/full502/final_project_team1/core/service/AssignmentServiceImpl.java
package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
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

    /**
     * keyword="강동" → lot_address LIKE %강동% 인 건물들 대상으로
     * 기존 같은 지역 배정 삭제 후, 사용자들에 라운드로빈 배정
     */
    @Override
    @Transactional
    public int assignRegionRoundRobin(String keyword) {
        // 1) 기존 같은 지역 배정 제거 (Repository에 메서드가 있어야 합니다)
        assignRepo.deleteAllByLotAddressLike(keyword);

        // 2) 후보 건물/유저 목록
        List<BuildingEntity> buildings = buildingRepo.findByLotAddressLike(keyword);
        List<UserAccountEntity> users = userRepo.findAll(Sort.by(Sort.Direction.ASC, "userId"));

        if (users.isEmpty() || buildings.isEmpty()) return 0;

        // 3) 라운드로빈 배정
        int u = users.size();
        int created = 0;
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < buildings.size(); i++) {
            BuildingEntity b = buildings.get(i);
            UserAccountEntity user = users.get(i % u);

            UserBuildingAssignmentEntity a = UserBuildingAssignmentEntity.builder()
                .buildingId(b.getId())
                .user(user)
                .assignedAt(now)
                .build();

            assignRepo.save(a);
            created++;
        }
        return created;
    }

    /** 유저별 배정 목록을 (buildingId, lotAddress) 맵으로 반환 */
    @Override
    public List<Map<String, Object>> getAssignments(Integer userId) {
        // Repository에 findPairsByUserId(userId) : List<Object[]> 필요
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

    /** 지정 사용자에게 여러 건물을 일괄 배정 */
    @Override
    @Transactional
    public int assignToUser(Integer userId, List<Long> buildingIds) {
        if (userId == null) throw new IllegalArgumentException("userId는 필수입니다.");
        if (buildingIds == null || buildingIds.isEmpty()) return 0;

        UserAccountEntity user = userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자: " + userId));

        int created = 0;
        LocalDateTime now = LocalDateTime.now();

        // (선택) 기존 같은 건물 배정 제거가 필요하다면 Repository에 메서드 추가 후 호출
        // assignRepo.deleteAllByBuildingIdIn(buildingIds);

        for (Long bid : buildingIds) {
            UserBuildingAssignmentEntity a = UserBuildingAssignmentEntity.builder()
                .buildingId(bid)
                .user(user)
                .assignedAt(now)
                .build();
            assignRepo.save(a);
            created++;
        }
        return created;
    }
}
