package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.DashboardStatsAppDTO;
import bitc.full502.final_project_team1.api.web.dto.DashboardStatsDTO;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserBuildingAssignmentRepository;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

 import java.time.LocalDate;
 import java.time.LocalDateTime;
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

        // 4) 조사 진행 중 = 배정 - (SENT + APPROVED + REJECTED)
        long inProgress = assigned - (waiting + approved);

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

    /** ✅ 앱용 (로그인된 조사자 기준) */
    @Override
    public DashboardStatsAppDTO getStats(Long userId) {
        // 1. 전체 배정 건수 (user 기준)
        long totalAssignments =
                assignmentRepo.countByUser_UserIdAndStatus(userId, 1) +
                        assignmentRepo.countByUser_UserIdAndStatus(userId, 2) +
                        assignmentRepo.countByUser_UserIdAndStatus(userId, 3) +
                        assignmentRepo.countByUser_UserIdAndStatus(userId, 4);

        // 2. 결재 완료 건수
        long approved = surveyResultRepo.countByUser_UserIdAndStatus(userId, "APPROVED");

        // 3. 결재 대기 건수
        long waitingApproval = surveyResultRepo.countByUser_UserIdAndStatus(userId, "SENT");

        // 4. 금일 완료 (오늘 전송한 SENT)
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayComplete = surveyResultRepo.countSentToday(userId, todayStart);

        // 5. 조사 진행 중
        long tempResults = surveyResultRepo.countByUser_UserIdAndStatus(userId, "TEMP");
        long assignedWithoutResult = assignmentRepo.findAssignedWithoutAnyResult(userId).size();
        long inProgress = tempResults + assignedWithoutResult;

        // 6. 진행률 계산
        double progressRate = 0.0;
        long approvedAndSent = approved + waitingApproval;
        if (totalAssignments > 0) {
            progressRate = ((double) approvedAndSent / totalAssignments) * 100;
        }

        return DashboardStatsAppDTO.builder()
                .progressRate(Math.round(progressRate * 10) / 10.0)
                .total(approved)          // 총 건수 = approved 누적
                .todayComplete(todayComplete)
                .inProgress(inProgress)
                .waitingApproval(waitingApproval)
                .approved(approved)
                .build();
    }

}
