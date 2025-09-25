package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.api.app.dto.AssignedBuildingDto;
import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserBuildingAssignmentRepository extends JpaRepository<UserBuildingAssignmentEntity, Long> {

  /** 유저의 배정 목록 (building_id, lot_address) DTO 프로젝션 없이 Map/DTO는 서비스/컨트롤러에서 조립 */
  @Query("""
           select a.buildingId, b.lotAddress
           from UserBuildingAssignmentEntity a
           join a.building b
           where a.user.userId = :userId
           order by a.buildingId
           """)
    List<Object[]> findPairsByUserId(Long userId);

  /** 특정 지역(키워드) 배정만 삭제 (라운드로빈 재배정 전에 사용) */
  @Modifying
  @Query("""
           delete from UserBuildingAssignmentEntity a
           where a.buildingId in (
                select b.id from BuildingEntity b
                where b.lotAddress like %:keyword%
           )
           """)
  int deleteAllByLotAddressLike(String keyword);

    long countByUser_UserIdAndStatus(Long userId, Integer status);


  @Query("SELECT COUNT(u) FROM UserBuildingAssignmentEntity u")
  long countAllAssignments();

  @Query("SELECT COUNT(u) FROM UserBuildingAssignmentEntity u WHERE u.status = :status")
  long countByStatus(@Param("status") int status);

  Optional<UserBuildingAssignmentEntity> findByBuildingId(Long buildingId);

  // 경합 상황 방어 (선택): 행 잠금 후 읽기
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from UserBuildingAssignmentEntity u where u.buildingId = :buildingId")
  Optional<UserBuildingAssignmentEntity> findByBuildingIdForUpdate(@Param("buildingId") Long buildingId);

  public interface PendingApprovalRow {
    Long   getId();
    String getLotAddress();
    String getRoadAddress();
    String getBuildingName();

    // ⬇⬇ 컨트롤러에서 쓰는 이름과 '정확히' 일치해야 함
    Long   getAssignedUserId();
    String getAssignedName();
    String getAssignedUsername();

    Long   getApprovalId();
  }

  @Query(value = """
    SELECT
      b.id                         AS id,
      b.lot_address                AS lotAddress,
      b.road_address               AS roadAddress,
      b.building_name              AS buildingName,
      uba.user_id                  AS assignedUserId,
      COALESCE(u.name, u.username) AS assignedName,
      u.username                   AS assignedUsername,
      uba.approval_id              AS approvalId
    FROM user_building_assignment uba
    JOIN building b       ON b.id = uba.building_id
    LEFT JOIN user_account u ON u.user_id = uba.user_id
    WHERE uba.user_id IS NOT NULL
      AND uba.approval_id IS NULL
      AND (:emd IS NULL OR b.lot_address LIKE CONCAT('%', :emd, '%'))
    ORDER BY b.id DESC
  """, nativeQuery = true)
  List<PendingApprovalRow> findAssignedWithoutApprover(@Param("emd") String eupMyeonDong);

  @Modifying
  @Transactional
  @Query(value = "DELETE FROM user_building_assignment WHERE building_id = :buildingId", nativeQuery = true)
  int deleteByBuildingId(@Param("buildingId") Long buildingId);
}