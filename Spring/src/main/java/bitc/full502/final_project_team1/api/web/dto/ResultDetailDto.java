// src/main/java/.../web/dto/ResultDetailDto.java
package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResultDetailDto {
    private Long id;
    private String caseNo;
    private String investigator;
    private String address;
    private String status;

    // 내부/외부 점검 항목
    private Integer possible, adminUse, idleRate, safety;
    private Integer wall, roof, windowState, parking;
    private Integer entrance, ceiling, floor;

    // 사진
    private String extPhoto, extEditPhoto, intPhoto, intEditPhoto;

    public static ResultDetailDto from(SurveyResultEntity e) {
        var u = e.getUser();
        var b = e.getBuilding();
        return ResultDetailDto.builder()
                .id(e.getId())
                .caseNo("M-" + e.getId())
                .investigator(u == null ? null :
                        (u.getName() != null ? u.getName() : u.getUsername()))
                .address(b == null ? null : b.getLotAddress())
                .status(e.getStatus())
                .possible(e.getPossible())
                .adminUse(e.getAdminUse())
                .idleRate(e.getIdleRate())
                .safety(e.getSafety())
                .wall(e.getWall())
                .roof(e.getRoof())
                .windowState(e.getWindowState())
                .parking(e.getParking())
                .entrance(e.getEntrance())
                .ceiling(e.getCeiling())
                .floor(e.getFloor())
                .extPhoto(e.getExtPhoto())
                .extEditPhoto(e.getExtEditPhoto())
                .intPhoto(e.getIntPhoto())
                .intEditPhoto(e.getIntEditPhoto())
                .build();
    }
}
