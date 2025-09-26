package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    // 기타 사항
    private String extEtc;
    private String intEtc;

    // 🔹 지도 좌표 추가
    private Double latitude;
    private Double longitude;

    public static ResultDetailDto from(SurveyResultEntity e) {
        var u = e.getUser();
        var b = e.getBuilding();

        return ResultDetailDto.builder()
                .id(e.getId())
                .caseNo("M-" + e.getId())
                .investigator(u == null ? "조사원 없음" :
                        (u.getName() != null ? u.getName() : u.getUsername()))
                .address(b == null ? "주소 없음" : b.getLotAddress())
                .status(e.getStatus() != null ? e.getStatus() : "미정")

                // null 값 → 0 으로 변환
                .possible(e.getPossible() != null ? e.getPossible() : 0)
                .adminUse(e.getAdminUse() != null ? e.getAdminUse() : 0)
                .idleRate(e.getIdleRate() != null ? e.getIdleRate() : 0)
                .safety(e.getSafety() != null ? e.getSafety() : 0)
                .wall(e.getWall() != null ? e.getWall() : 0)
                .roof(e.getRoof() != null ? e.getRoof() : 0)
                .windowState(e.getWindowState() != null ? e.getWindowState() : 0)
                .parking(e.getParking() != null ? e.getParking() : 0)
                .entrance(e.getEntrance() != null ? e.getEntrance() : 0)
                .ceiling(e.getCeiling() != null ? e.getCeiling() : 0)
                .floor(e.getFloor() != null ? e.getFloor() : 0)

                // 사진 / null → "" 로 변환
                .extPhoto(e.getExtPhoto() != null ? e.getExtPhoto() : "")
                .extEditPhoto(e.getExtEditPhoto() != null ? e.getExtEditPhoto() : "")
                .intPhoto(e.getIntPhoto() != null ? e.getIntPhoto() : "")
                .intEditPhoto(e.getIntEditPhoto() != null ? e.getIntEditPhoto() : "")

                // 기타 사항
                .extEtc(e.getExtEtc())
                .intEtc(e.getIntEtc())

                // 🔹 좌표값 추가 (건물 엔티티에 값이 있을 경우만)
                .latitude(b != null ? b.getLatitude() : null)
                .longitude(b != null ? b.getLongitude() : null)

                .build();
    }
}
