package bitc.full502.final_project_team1.web.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "building")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 순번

    private String lotAddress;          // 번지주소
    private String lotMainNo;           // 번
    private String lotSubNo;            // 지
    private String roadAddress;         // 도로명주소

    private String ledgerDivisionName;  // 대장구분코드명
    private String ledgerTypeName;      // 대장종류코드명
    private String buildingName;        // 건물명
    private Integer extraLotCount;      // 외필지수

    private String newRoadCode;         // 새주소도로코드
    private String newLegalDongCode;    // 새주소법정동코드
    private String newMainNo;           // 새주소본번
    private String newSubNo;            // 새주소부번

    private String mainSubCode;         // 주부속구분코드
    private String mainSubName;         // 주부속구분코드명

    private Double landArea;            // 대지면적(㎡)
    private Double buildingArea;        // 건축면적(㎡)
    private Double buildingCoverage;    // 건폐율(%)
    private Double totalFloorArea;      // 연면적(㎡)
    private Double floorAreaForRatio;   // 용적률산정연면적(㎡)
    private Double floorAreaRatio;      // 용적률(%)

    private String structureCode;       // 구조코드
    private String structureName;       // 구조코드명
    private String etcStructure;        // 기타구조

    private String mainUseCode;         // 주용도코드
    private String mainUseName;         // 주용도코드명
    private String etcUse;              // 기타용도

    private String roofCode;            // 지붕코드
    private String roofName;            // 지붕코드명
    private String etcRoof;             // 기타지붕

    private Double height;              // 높이(m)
    private Integer groundFloors;       // 지상층수
    private Integer basementFloors;     // 지하층수
    private Integer passengerElevators; // 승용승강기수
    private Integer emergencyElevators; // 비상용승강기수

    private Integer annexCount;         // 부속건축물수
    private Double annexArea;           // 부속건축물면적(㎡)
    private Double totalBuildingArea;   // 총동연면적(㎡)

    private Double latitude;            // 위도
    private Double longitude;           // 경도
}
