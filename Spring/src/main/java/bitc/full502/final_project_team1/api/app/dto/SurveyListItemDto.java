package bitc.full502.final_project_team1.api.app.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyListItemDto {
    private Long surveyId;
    private Long buildingId;
    private String address;
    private String buildingName;
    private String status;        // REJECTED / APPROVED / SENT / TEMP
    private String updatedAtIso;  // 정렬/표시용
    private String rejectReason;  // REJECTED일 때만 채워도 OK
}
