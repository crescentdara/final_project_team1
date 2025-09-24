package bitc.full502.final_project_team1.api.app.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsAppDTO {
    private double progressRate;   // 진행률 (%)
    private long total;            // 총 건수 (APPROVED)
    private long todayComplete;    // 금일 완료 (SENT)
    private long inProgress;       // 조사 진행
    private long waitingApproval;  // 결재 대기
    private long approved;         // 결재 완료
}
