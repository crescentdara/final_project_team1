package bitc.full502.final_project_team1.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "survey_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. 조사불가여부 (1 or 2)
    private Integer possible;

    // 2. 행정목적활용여부 (1=활용, 2=일부활용, 3=미활용)
    private Integer adminUse;

    // 3. 유휴비율 (1~4)
    private Integer idleRate;

    // 4. 안전등급 (1~5)
    private Integer safety;

    // 5. 외부상태 - 외벽 (1~3)
    private Integer wall;

    // 6. 외부상태 - 옥상 (1~3)
    private Integer roof;

    // 7. 외부상태 - 창호 (1~3)
    private Integer windowState;

    // 8. 외부상태 - 주차 가능 여부 (1 or 2)
    private Integer parking;

    // 9. 내부상태 - 현관 (1~3)
    private Integer entrance;

    // 10. 내부상태 - 천장 (1~3)
    private Integer ceiling;

    // 11. 내부상태 - 바닥 (1~3)
    private Integer floor;

    // ✅ 외부상태 기타사항
    @Column(name = "ext_etc", length = 500)
    private String extEtc;

    // ✅ 내부상태 기타사항
    @Column(name = "int_etc", length = 500)
    private String intEtc;

    // 12. 외부사진 경로
    private String extPhoto;

    // 13. 외부편집사진 경로
    private String extEditPhoto;

    // 14. 내부사진 경로
    private String intPhoto;

    // 15. 내부편집사진 경로
    private String intEditPhoto;

    // 16. 상태 (TEMP, SENT 등)
    private String status;

    // 건물
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id")
    private BuildingEntity building;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserAccountEntity user;

}
