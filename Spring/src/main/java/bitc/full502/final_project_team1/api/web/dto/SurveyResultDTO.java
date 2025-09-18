package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyResultDTO {

    private Long id;            // 조사 결과 PK
    private String buildingName; // 건물명
    private String userName;     // 조사자 이름
    private String status;       // 상태 (TEMP, SENT 등)

    // ✅ Entity → DTO 변환 메서드
    public static SurveyResultDTO fromEntity(SurveyResultEntity entity) {
        return SurveyResultDTO.builder()
                .id(entity.getId())
                .buildingName(entity.getBuilding().getBuildingName())
                .userName(entity.getUser().getName())
                .status(entity.getStatus())
                .build();
    }
}
