package bitc.full502.final_project_team1.core.domain.entity;

<<<<<<< HEAD
=======
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
>>>>>>> origin/web/his/MergedTotalSurveyListSearch
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

<<<<<<< HEAD
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

    // 작성일 (자동 저장, 수정 불가)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정일 (null 허용)
=======
    @Column(name = "possible")
    private Integer possible;

    @Column(name = "admin_use")
    private Integer adminUse;

    @Column(name = "idle_rate")
    private Integer idleRate;

    @Column(name = "safety")
    private Integer safety;

    @Column(name = "wall")
    private Integer wall;

    @Column(name = "roof")
    private Integer roof;

    @Column(name = "window_state")
    private Integer windowState;

    @Column(name = "parking")
    private Integer parking;

    @Column(name = "entrance")
    private Integer entrance;

    @Column(name = "ceiling")
    private Integer ceiling;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "ext_photo")
    private String extPhoto;

    @Column(name = "ext_edit_photo")
    private String extEditPhoto;

    @Column(name = "int_photo")
    private String intPhoto;

    @Column(name = "int_edit_photo")
    private String intEditPhoto;

    @Column(name = "status")
    private String status;

    @Column(name = "ext_etc", length = 500)
    private String extEtc;

    @Column(name = "int_etc", length = 500)
    private String intEtc;

    // ✅ 날짜 필드 (중복 제거)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

>>>>>>> origin/web/his/MergedTotalSurveyListSearch
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

<<<<<<< HEAD

=======
    // 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id")
    private BuildingEntity building;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserAccountEntity user;
>>>>>>> origin/web/his/MergedTotalSurveyListSearch
}
