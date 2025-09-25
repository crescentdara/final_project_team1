package bitc.full502.final_project_team1.api.app.dto;

import lombok.Data;

@Data
public class AppSurveyResultRequest {
    private Long surveyId;
    private Integer possible;
    private Integer adminUse;
    private Integer idleRate;
    private Integer safety;
    private Integer wall;
    private Integer roof;
    private Integer windowState;
    private Integer parking;
    private Integer entrance;
    private Integer ceiling;
    private Integer floor;
    private String extEtc;
    private String intEtc;
    private String extPhoto;
    private String extEditPhoto;
    private String intPhoto;
    private String intEditPhoto;
    private String status;
    private Long buildingId;
    private Long userId;
}
