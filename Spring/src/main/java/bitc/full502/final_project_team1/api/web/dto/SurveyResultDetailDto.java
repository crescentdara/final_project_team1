package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyResultDetailDto {

    private Long id;

    // 조사 항목들
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

    // 사진
    private String extPhoto;
    private String extEditPhoto;
    private String intPhoto;
    private String intEditPhoto;

    // 상태
    private String status;

    // 기타사항
    private String extEtc;
    private String intEtc;

    // 관계 정보
    private String buildingName;
    private String userName;

    // Entity → DTO 변환 메서드
    public static SurveyResultDetailDto fromEntity(SurveyResultEntity entity) {
        return SurveyResultDetailDto.builder()
                .id(entity.getId())
                .possible(entity.getPossible())
                .adminUse(entity.getAdminUse())
                .idleRate(entity.getIdleRate())
                .safety(entity.getSafety())
                .wall(entity.getWall())
                .roof(entity.getRoof())
                .windowState(entity.getWindowState())
                .parking(entity.getParking())
                .entrance(entity.getEntrance())
                .ceiling(entity.getCeiling())
                .floor(entity.getFloor())
                .extPhoto(entity.getExtPhoto())
                .extEditPhoto(entity.getExtEditPhoto())
                .intPhoto(entity.getIntPhoto())
                .intEditPhoto(entity.getIntEditPhoto())
                .status(entity.getStatus())
                .extEtc(entity.getExtEtc())
                .intEtc(entity.getIntEtc())
                .buildingName(entity.getBuilding().getBuildingName())
                .userName(entity.getUser().getName())
                .build();
    }
}
