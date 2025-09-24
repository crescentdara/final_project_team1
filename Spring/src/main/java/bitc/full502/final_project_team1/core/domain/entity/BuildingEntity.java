package bitc.full502.final_project_team1.core.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "building",
    indexes = {
        @Index(name = "idx_building_assigned_user", columnList = "assigned_user_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 순번

    private String lotAddress;
    private String lotMainNo;
    private String lotSubNo;
    private String roadAddress;

    private String ledgerDivisionName;
    private String ledgerTypeName;
    private String buildingName;
    private Integer extraLotCount;

    private String newRoadCode;
    private String newLegalDongCode;
    private String newMainNo;
    private String newSubNo;

    private String mainSubCode;
    private String mainSubName;

    private Double landArea;
    private Double buildingArea;
    private Double buildingCoverage;
    private Double totalFloorArea;
    private Double floorAreaForRatio;
    private Double floorAreaRatio;

    private String structureCode;
    private String structureName;
    private String etcStructure;

    private String mainUseCode;
    private String mainUseName;
    private String etcUse;

    private String roofCode;
    private String roofName;
    private String etcRoof;

    private Double height;
    private Integer groundFloors;
    private Integer basementFloors;
    private Integer passengerElevators;
    private Integer emergencyElevators;

    private Integer annexCount;
    private Double annexArea;
    private Double totalBuildingArea;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private Integer status;  // 0 = 미배정, 1 = 배정됨

    @Column(name = "assigned_user_id", insertable = false, updatable = false)
    private Long assignedUserId;

    /** ✅ 결재자(배정 사용자) : null이면 미배정 */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
        name = "assigned_user_id", // FK 컬럼명 (INT)
        foreignKey = @ForeignKey(name = "fk_building_assigned_user")
    )
    @ToString.Exclude
    @JsonIgnore
    private UserAccountEntity assignedUser;

    /** ✅ 편의: 배정 여부 */
    @Transient
    public boolean isAssigned() {
        return assignedUser != null;
    }

    /** ✅ 편의: 배정자 PK (null-safe) */
    @Transient
    public Long getAssignedUserIdSafe() {
        return (assignedUser != null) ? assignedUser.getUserId() : null;
    }

    /** ✅ 편의: 배정자 표시명 (null-safe) */
    @Transient
    public String getAssignedUserNameSafe() {
        if (assignedUser == null) return null;
        String n = assignedUser.getName();
        return (n != null && !n.isBlank()) ? n : assignedUser.getUsername();
    }

    /** ✅ status(0/1)와 자동 동기화(선택) */
    @PrePersist @PreUpdate
    public void syncStatus() {
        this.status = (this.assignedUser != null) ? 1 : 0;
    }

    @JsonProperty("assignedName")
    public String getAssignedNameJson() {
        if (assignedUser == null) return null;
        // 이름이 비어있으면 username을 대체로
        String n = assignedUser.getName();
        return (n != null && !n.isBlank()) ? n : assignedUser.getUsername();
    }

    @JsonProperty("assignedUsername")   // (선택) 아이디도 함께 내리고 싶다면
    public String getAssignedUsernameJson() {
        return assignedUser != null ? assignedUser.getUsername() : null;
    }
}
