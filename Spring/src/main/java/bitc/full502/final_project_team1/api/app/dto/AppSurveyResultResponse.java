package bitc.full502.final_project_team1.api.app.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// 응답용 (프리필/상세)
@Builder
@Data
public class AppSurveyResultResponse {
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

    // 서버에 이미 저장된 이미지 URL(있으면 프리필 시 완료로 인정)
    private String extPhoto;
    private String extEditPhoto;
    private String intPhoto;
    private String intEditPhoto;

    private String status;       // TEMP / SENT / REJECTED / APPROVED
    private Long buildingId;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 빌딩 주소
    private String buildingAddress;
}

