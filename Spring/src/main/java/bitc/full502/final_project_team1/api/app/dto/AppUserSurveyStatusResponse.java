package bitc.full502.final_project_team1.api.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppUserSurveyStatusResponse {
    private Long approved; // 결재완료 (APPROVED)
    private Long rejected; // 반려 (REJECTED)
    private Long sent;     // 전송완료 (SENT)
    private Long temp;     // 임시저장 (TEMP)
}
