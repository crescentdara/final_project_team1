package bitc.full502.final_project_team1.api.web.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private double progressRate;   // 진행률 (%)
    private long inProgress;       // 조사 진행 중 (status = 1)
    private long waitingApproval;  // 결재 대기 중 (status = 2)
    private long approved;         // 결재 완료 (status = 3)

    private long totalBuildings;
    private long assignedBuildings;

}
