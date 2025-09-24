package bitc.full502.final_project_team1.api.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppBuildingDetailDto {
    private Long id;

    private String lotAddress;      // 번지주소

    private String buildingName;    // 건물명
    private Integer groundFloors;   // 지상층수
    private Integer basementFloors;
    private Double totalFloorArea;  // 연면적
    private Double landArea;        // 대지면적

    private String mainUseCode;     // 주용도코드
    private String mainUseName;     // 주용도코드명
    private String etcUse;          // 기타용도

    private String structureName;   // 구조명
    private Double height;          // 높이(m)
}