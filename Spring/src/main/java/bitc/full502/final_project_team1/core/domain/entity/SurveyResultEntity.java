package bitc.full502.final_project_team1.core.domain.entity;

import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

    // 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id")
    private BuildingEntity building;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserAccountEntity user;

    /** 승인한 사람 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", foreignKey = @ForeignKey(name = "fk_sr_approver_user"))
    private UserAccountEntity approver;
}
