package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.web.dto.DashboardStatsDTO;
import bitc.full502.final_project_team1.core.domain.repository.UserBuildingAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserBuildingAssignmentRepository assignmentRepo;

    @Override
    public DashboardStatsDTO getStats() {
        Long total = assignmentRepo.countAllAssignments();
        Long inProgress = assignmentRepo.countByStatus(1);   // 조사 진행 중
        Long waiting = assignmentRepo.countByStatus(2);      // 결재 대기
        Long approved = assignmentRepo.countByStatus(3);     // 결재 완료

        double progressRate = 0.0;
        if (total > 0) {
            progressRate = ((double) approved / total) * 100; // ✅ 결재 완료 기준
        }

        return DashboardStatsDTO.builder()
                .progressRate(Math.round(progressRate * 10) / 10.0) // 소수점 1자리
                .inProgress(inProgress)
                .waitingApproval(waiting)
                .approved(approved)
                .build();
    }

}
