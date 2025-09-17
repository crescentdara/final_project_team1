package bitc.full502.final_project_team1.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_building_assignment", schema = "java502_team1_final_db")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserBuildingAssignmentEntity {

    /** PK = building_id (건물 1개당 배정 1개) */
    @Id
    @Column(name = "building_id")
    private Long buildingId;

    /** 읽기용으로 건물 엔티티도 매핑(동일 컬럼을 공유) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", referencedColumnName = "id", insertable = false, updatable = false)
    private BuildingEntity building;

    /** 유저(N) : 배정(1) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserAccountEntity user;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @PrePersist
    void onCreate() {
        if (assignedAt == null) assignedAt = LocalDateTime.now();
    }

    @Column(nullable = false)
    private Integer status;  // assigned = 1 / unassigned = 2

}
