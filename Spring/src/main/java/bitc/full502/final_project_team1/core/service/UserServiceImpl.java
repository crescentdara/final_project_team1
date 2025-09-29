package bitc.full502.final_project_team1.core.service.impl;

import bitc.full502.final_project_team1.api.web.dto.UserUpdateDto;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.repository.*;
import bitc.full502.final_project_team1.core.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserAccountRepository userRepo;
    private final UserBuildingAssignmentRepository ubaRepo;
    private final SurveyResultRepository surveyResultRepo;
    private final BuildingRepository buildingRepo;
    private final ApprovalRepository approvalRepo;

    /**
     * ✅ 조사원 정보 수정
     */
    @Transactional
    @Override
    public void updateUser(Long userId, UserUpdateDto dto) {
        UserAccountEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.setName(dto.getName());
        user.setPreferredRegion(dto.getPreferredRegion());
        user.setRole(dto.getRole());
        user.setUsername(dto.getUsername());

        userRepo.save(user);
    }

    /**
     * ✅ 조사원 삭제
     */
    @Transactional
    @Override
    public void deleteUser(Long userId) {
        // 1. 배정된 건물 목록 조회
        var assignments = ubaRepo.findByUser_UserId(userId);

        assignments.forEach(uba -> {
            Long buildingId = uba.getBuilding().getId(); // ✅ PK는 buildingId 아님, id

            var surveyResult = surveyResultRepo.findTop1ByBuilding_IdOrderByCreatedAtDesc(buildingId);

            if (surveyResult == null) {
                // 설문 결과가 없으면 단순 배정 삭제
                ubaRepo.delete(uba);
                return;
            }

            String status = surveyResult.getStatus();  // 문자열 기준

            switch (status) {
                case "APPROVED":
                    // 아무것도 안 함 (히스토리 유지)
                    break;

                case "SENT":
                    // 그대로 둠 (결재 대기 중)
                    break;

                case "TEMP":
                case "REJECTED":
                    // 1) 배정 삭제
                    ubaRepo.delete(uba);
                    // 2) 설문 결과 삭제
                    surveyResultRepo.delete(surveyResult);
                    // 3) 건물 상태 0으로 변경
                    buildingRepo.updateStatus(buildingId, 0);
                    // 4) approval 삭제
                    approvalRepo.deleteByBuilding_Id(buildingId);
                    break;

                default:
                    throw new IllegalStateException("알 수 없는 상태값: " + status);
            }
        });

        // 마지막으로 user 삭제
        userRepo.deleteById(userId);
    }
}
