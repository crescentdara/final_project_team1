package bitc.full502.final_project_team1.api.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildingDTO {
    private String lotAddress;     // 번지주소
    private Double latitude;       // 위도
    private Double longitude;      // 경도
    private String buildingName;   // 건물명
    private String mainUseName;        // 주용도
    private String structureName;      // 구조명
    private Integer groundFloors;  // 지상층수
    private Integer basementFloors;// 지하층수
    private Double landArea;       // 대지면적
    private Double buildingArea;   // 건축면적
}
