package bitc.full502.final_project_team1.api.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class AppUserSurveyStatsResponse {
    private long scheduled;   // 조사예정
    private long waiting;     // 결재대기
    private long rejected;    // 반려(재조사대상)
    private long tempSaved;   // 미전송(임시저장)
}
