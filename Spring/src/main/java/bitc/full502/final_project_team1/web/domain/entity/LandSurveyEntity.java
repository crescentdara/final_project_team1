package bitc.full502.final_project_team1.web.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "land_survey")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandSurveyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private String projectId;   // 사업지구번호

    @Column(name = "standard_year")
    private Integer standardYear;   // 기준년도

    @Column(name = "region_code")
    private String regionCode;  // 시군구코드

    @Column(name = "region_name")
    private String regionName;  // 시군구명

    @Column(name = "project_name")
    private String projectName; // 사업지구명

    @Column(name = "land_unique_code")
    private String landUniqueCode; // 토지고유코드

    @Column(name = "land_type_code")
    private String landTypeCode;   // 토지임야대장 지목코드

    @Column(name = "land_area")
    private Double landArea;       // 토지임야대장 면적

    // 조사자 의견내용 (빼고 싶다고 하셔서 제외)

    @Column(name = "building_serial")
    private String buildingSerial; // 건축물 일련번호

    @Column(name = "floor_above")
    private Integer floorAbove;    // 지상층수

    @Column(name = "floor_below")
    private Integer floorBelow;    // 지하층수

    @Column(name = "building_area")
    private Double buildingArea;   // 건축면적

    @Column(name = "building_coverage")
    private Double buildingCoverage; // 건폐율

    @Column(name = "floor_area_ratio")
    private Double floorAreaRatio;   // 용적률

    @Column(name = "structure_code")
    private String structureCode;    // 건축물구조코드

    @Column(name = "structure_name")
    private String structureName;    // 건축물구조코드명

    @Column(name = "usage_code")
    private String usageCode;        // 건축물용도코드

    @Column(name = "usage_name")
    private String usageName;        // 건축물용도코드명
}
