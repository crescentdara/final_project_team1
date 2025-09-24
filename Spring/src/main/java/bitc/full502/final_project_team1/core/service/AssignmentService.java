package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.core.domain.entity.*;
import bitc.full502.final_project_team1.core.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final BuildingRepository buildingRepo;
    private final UserAccountRepository userRepo;
    private final UserBuildingAssignmentRepository assignRepo;

    /**
     * 예: keyword="강동" → lot_address에 '강동'이 포함된 건물들을
     *    user_account를 기준으로 라운드로빈 배정(고유)한다.
     * 기존 같은 지역 배정은 삭제하고 다시 채운다.
     * @return 생성된 배정 수
     */
    @Transactional
    public int assignRegionRoundRobin(String keyword) {
        // 1) 기존 같은 지역 배정 제거
        assignRepo.deleteAllByLotAddressLike(keyword);

        // 2) 후보 건물/유저 목록
        List<BuildingEntity> buildings = buildingRepo.findByLotAddressLike(keyword);
        List<UserAccountEntity> users = userRepo.findAll(Sort.by(Sort.Direction.ASC, "userId"));

        if (users.isEmpty() || buildings.isEmpty()) return 0;

        // 3) 라운드로빈 배정 (건물 1개당 유저 1명 → building_id가 PK라 중복 불가)
        int u = users.size();
        int created = 0;
        for (int i = 0; i < buildings.size(); i++) {
            BuildingEntity b = buildings.get(i);
            UserAccountEntity user = users.get(i % u);

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
}