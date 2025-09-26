package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.web.dto.DashboardStatsDTO;
import bitc.full502.final_project_team1.core.domain.repository.UserBuildingAssignmentRepository;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserBuildingAssignmentRepository assignmentRepo;
    private final SurveyResultRepository surveyResultRepo;
    private final BuildingRepository buildingRepo;

    @Override
    public DashboardStatsDTO getStats() {
        // 1) 건물 전체 수, 배정된 건물 수
        long totalBuildings = buildingRepo.count();
        long assignedBuildings = buildingRepo.countByStatus(1);

        // 2) 전체 배정된 건수 (status ∈ (1,2)) - 조사 단위
        long assigned = assignmentRepo.countByStatusIn(Arrays.asList(1, 2));

        // 3) survey_result 기준 상태별 카운트
        long waiting   = surveyResultRepo.countByStatus("SENT");      // 결재 대기
        long approved  = surveyResultRepo.countByStatus("APPROVED");  // 결재 완료
        long rejected  = surveyResultRepo.countByStatus("REJECTED");  // 반려

        // 4) 조사 진행 중 = 배정 - (SENT + APPROVED + REJECTED)
        long inProgress = assigned - (waiting + approved + rejected);

        // 5) 진행률 = 결재 완료 / 배정
        double progressRate = 0.0;
        if (assigned > 0) {
            progressRate = ((double) approved / assigned) * 100;
        }

        return DashboardStatsDTO.builder()
                .progressRate(Math.round(progressRate * 10) / 10.0) // 소수점 1자리
                .inProgress(inProgress)
                .waitingApproval(waiting)
                .approved(approved)
                .totalBuildings(totalBuildings)          // ✅ 추가
                .assignedBuildings(assignedBuildings)    // ✅ 추가
                .build();
    }
}
