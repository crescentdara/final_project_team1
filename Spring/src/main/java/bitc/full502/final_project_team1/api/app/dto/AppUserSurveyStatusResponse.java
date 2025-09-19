package bitc.full502.final_project_team1.api.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppUserSurveyStatusResponse {
    private long approved; // 결재완료 (APPROVED)
    private long rejected; // 반려 (REJECTED)
    private long sent;     // 전송완료 (SENT)
    private long temp;     // 임시저장 (TEMP)
}
