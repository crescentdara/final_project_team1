package bitc.full502.final_project_team1.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_building_assignment",
    schema = "java502_team1_final_db",
    uniqueConstraints = {
        // 건물 1개당 배정 1개를 강제
        @UniqueConstraint(name = "uk_uba_building", columnNames = "building_id")
    },
    indexes = {
        @Index(name = "idx_uba_user", columnList = "user_id"),
        @Index(name = "idx_uba_building", columnList = "building_id"),
        @Index(name = "idx_uba_approval", columnList = "approval_id"),
        @Index(name = "idx_uba_status", columnList = "status")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserBuildingAssignmentEntity {

    /** PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK: building.id (NOT NULL + UNIQUE 로 건물당 1배정 보장) */
    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    /** 읽기 전용 연관 (동일 컬럼 공유) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "building_id",
        referencedColumnName = "id",
        insertable = false,
        updatable = false,
        foreignKey = @ForeignKey(name = "fk_uba_building")
    )
    private BuildingEntity building;

    /** FK: user_account.id (배정 대상 유저/결재자) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_uba_user")
    )
    private UserAccountEntity user;

    /** FK: approval.id (결재 엔터티) */
    @Column(name = "approval_id")
    private Long approvalId;

    /** 읽기 전용 연관 (동일 컬럼 공유) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "approval_id",
        referencedColumnName = "id",
        insertable = false,
        updatable = false,
        foreignKey = @ForeignKey(name = "fk_uba_approval")
    )
    private ApprovalEntity approval;

    /** 배정 시각 */
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    /**
     * 상태 코드
     * 1 = 배정 , 2 = 결재 대기, 3 = 결재 완료, 4 = 반려
     */
    @Column(nullable = false)
    private Integer status;

    @PrePersist
    void onCreate() {
        if (assignedAt == null) assignedAt = LocalDateTime.now();
        if (status == null) status = 1; // 기본 배정 상태
    }
}
