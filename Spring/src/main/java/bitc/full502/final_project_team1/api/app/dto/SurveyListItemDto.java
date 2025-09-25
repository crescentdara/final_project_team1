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
    private String rejectReason;  // REJECTED일 때만 채워도 OK
    private String assignedAtIso;
    private Double latitude;
    private Double longitude;

}
