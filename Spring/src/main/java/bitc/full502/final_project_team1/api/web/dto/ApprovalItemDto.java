// src/main/java/.../api/web/dto/ApprovalItemDto.java
package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalItemDto {

    // ===== 기본 정보 =====
    private Long id;
    private String caseNo;         // CHANGED: M-{id} 파생
    private String investigator;   // CHANGED: user.name or username
    private String address;        // CHANGED: building.lotAddress
    private String status;         // CHANGED: survey_result.status

    // ===== 점검 항목(상세와 동일하게) =====
    // CHANGED: ResultDetailDto와 동일 필드 추가
    private Integer possible, adminUse, idleRate, safety;
    private Integer wall, roof, windowState, parking;
    private Integer entrance, ceiling, floor;

    // ===== 사진 경로(상세와 동일하게) =====
    // CHANGED: ResultDetailDto와 동일 필드 추가
    private String extPhoto, extEditPhoto, intPhoto, intEditPhoto;

    private String submittedAt;

    // CHANGED: from(SurveyResultEntity)도 상세와 동일 매핑
    public static ApprovalItemDto from(SurveyResultEntity e) {
        var u = e.getUser();
        var b = e.getBuilding();
        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String submitted = (e.getCreatedAt() != null) ? e.getCreatedAt().format(fmt) : null;

        return ApprovalItemDto.builder()
                .id(e.getId())
                .caseNo("M-" + e.getId())
                .investigator(u == null ? null : (u.getName() != null ? u.getName() : u.getUsername()))
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
                .submittedAt(submitted)
                .build();
    }
}
