package bitc.full502.final_project_team1.api.app.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AppSurveyResultDetailDto {
    private Long id;
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

    // 기존에 업로드/편집했던 사진 URL (있으면 앱에서 미리보기로 채움)
    private String extPhoto;
    private String extEditPhoto;
    private String intPhoto;
    private String intEditPhoto;

    private String status;            // TEMP / SENT / REJECTED / APPROVED
    private Long buildingId;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
