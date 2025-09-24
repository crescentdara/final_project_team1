package bitc.full502.final_project_team1.api.web.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private double progressRate;   // 진행률 (%)
    private Long inProgress;       // 조사 진행 중 (status = 1)
    private Long waitingApproval;  // 결재 대기 중 (status = 2)
    private Long approved;         // 결재 완료 (status = 3)
}
