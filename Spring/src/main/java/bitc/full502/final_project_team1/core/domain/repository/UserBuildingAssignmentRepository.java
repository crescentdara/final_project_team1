package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBuildingAssignmentRepository extends JpaRepository<UserBuildingAssignmentEntity, Long> {

  /** 유저의 배정 목록 (building_id, lot_address) */
  @Query("""
           select a.buildingId, b.lotAddress
           from UserBuildingAssignmentEntity a
           join a.building b
           where a.user.userId = :userId
           order by a.buildingId
           """)
  List<Object[]> findPairsByUserId(@Param("userId") Long userId);

  /** 특정 지역(키워드) 배정 삭제 (라운드로빈 재배정 전에 사용) — JPQL */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
           delete from UserBuildingAssignmentEntity a
           where a.buildingId in (
                select b.id from BuildingEntity b
                where b.lotAddress like %:keyword%
           )
           """)
  int deleteAllByLotAddressLike(@Param("keyword") String keyword);

  /** 상태별 카운트 */
  Long countByUser_UserIdAndStatus(Long userId, Integer status);

  /** 단건 조회/삭제용 키 메서드들 */
  Optional<UserBuildingAssignmentEntity> findByBuildingId(Long buildingId);
  Optional<UserBuildingAssignmentEntity> findByBuildingIdAndUser_UserId(Long buildingId, Long userId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  void deleteByBuildingIdAndUser_UserId(Long buildingId, Long userId);

  /** 전체/상태 카운트 (집계용) */
  @Query("SELECT COUNT(u) FROM UserBuildingAssignmentEntity u")
  Long countAllAssignments();

  @Query("SELECT COUNT(u) FROM UserBuildingAssignmentEntity u WHERE u.status = :status")
  Long countByStatus(@Param("status") int status);

  /** 대시보드: status in (...) */
  @Query("SELECT COUNT(u) FROM UserBuildingAssignmentEntity u WHERE u.status IN :statuses")
  long countByStatusIn(@Param("statuses") List<Integer> statuses);

  /** 결과가 전혀 없는 배정만 (앱 첫 진행 대상 등) */
  @Query("""
           select uba
           from UserBuildingAssignmentEntity uba
           where uba.user.userId = :userId
             and not exists (
               select 1
               from SurveyResultEntity sr
               where sr.building.id = uba.buildingId
                 and sr.user.userId = :userId
             )
           order by uba.assignedAt desc
           """)
  List<UserBuildingAssignmentEntity> findAssignedWithoutAnyResult(@Param("userId") Long userId);

  /** 경합 방어: 행 잠금 후 읽기 */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from UserBuildingAssignmentEntity u where u.buildingId = :buildingId")
  Optional<UserBuildingAssignmentEntity> findByBuildingIdForUpdate(@Param("buildingId") Long buildingId);

  /** 네이티브 프로젝션 (결재 배정 대기 목록) */
  interface PendingApprovalRow {
    Long   getId();
    String getLotAddress();
    String getRoadAddress();
    String getBuildingName();
    Long   getAssignedUserId();
    String getAssignedName();
    String getAssignedUsername();
    Long   getApprovalId();
  }

//  @Query(value = """
//      SELECT
//        b.id                         AS id,
//        b.lot_address                AS lotAddress,
//        b.road_address               AS roadAddress,
//        b.building_name              AS buildingName,
//        uba.user_id                  AS assignedUserId,
//        COALESCE(u.name, u.username) AS assignedName,
//        u.username                   AS assignedUsername,
//        uba.approval_id              AS approvalId
//      FROM user_building_assignment uba
//      JOIN building b          ON b.id = uba.building_id
//      LEFT JOIN user_account u ON u.user_id = uba.user_id
//      WHERE uba.user_id IS NOT NULL
//        AND uba.approval_id IS NULL
//        AND (:emd IS NULL OR b.lot_address LIKE CONCAT('%', :emd, '%'))
//      ORDER BY b.id DESC
//      """, nativeQuery = true)
//  List<PendingApprovalRow> findAssignedWithoutApprover(@Param("emd") String eupMyeonDong);

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
      JOIN building b          ON b.id = uba.building_id
      LEFT JOIN user_account u ON u.user_id = uba.user_id
      LEFT JOIN survey_result sr ON sr.building_id = b.id AND sr.user_id = uba.user_id
      WHERE uba.user_id IS NOT NULL
        AND uba.approval_id IS NULL
        AND (:emd IS NULL OR b.lot_address LIKE CONCAT('%', :emd, '%'))
        AND (sr.status IS NULL OR sr.status <> 'APPROVED')
      ORDER BY b.id DESC
      """, nativeQuery = true)
  List<PendingApprovalRow> findAssignedWithoutApprover(@Param("emd") String eupMyeonDong);


  /** 네이티브: building_id 기준 일괄 삭제 */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = "DELETE FROM user_building_assignment WHERE building_id = :buildingId", nativeQuery = true)
  int deleteByBuildingId(@Param("buildingId") Long buildingId);

  /** 특정 조사원에게 배정된 모든 UBA */
  List<UserBuildingAssignmentEntity> findByUser_UserId(Long userId);
}






//package bitc.full502.final_project_team1.core.domain.repository;
//
//import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
//import jakarta.persistence.LockModeType;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Lock;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface UserBuildingAssignmentRepository extends JpaRepository<UserBuildingAssignmentEntity, Long> {
//
//  /** 유저의 배정 목록 (building_id, lot_address) DTO 프로젝션 없이 Map/DTO는 서비스/컨트롤러에서 조립 */
//  @Query("""
//           select a.buildingId, b.lotAddress
//           from UserBuildingAssignmentEntity a
//           join a.building b
//           where a.user.userId = :userId
//           order by a.buildingId
//           """)
//    List<Object[]> findPairsByUserId(Long userId);
//
//  /** 특정 지역(키워드) 배정만 삭제 (라운드로빈 재배정 전에 사용) */
//  @Modifying(clearAutomatically = true, flushAutomatically = true)
//  @Query("""
//         delete from UserBuildingAssignmentEntity a
//         where a.buildingId in (
//              select b.id from BuildingEntity b
//              where b.lotAddress like %:keyword%
//         )
//         """)
//  int deleteAllByLotAddressLike(String keyword);
//
//
//    Long countByUser_UserIdAndStatus(Long userId, Integer status);
//
//    Optional<UserBuildingAssignmentEntity> findByBuildingIdAndUser_UserId(Long buildingId, Long userId);
//
//    void deleteByBuildingIdAndUser_UserId(Long buildingId, Long userId);
//
//    @Query("SELECT COUNT(u) FROM UserBuildingAssignmentEntity u")
//    Long countAllAssignments();
//
//    @Query("SELECT COUNT(u) FROM UserBuildingAssignmentEntity u WHERE u.status = :status")
//    Long countByStatus(@Param("status") int status);
//
//    @Query("""
//    select uba
//    from UserBuildingAssignmentEntity uba
//    where uba.user.userId = :userId
//      and not exists (
//        select 1
//        from SurveyResultEntity sr
//        where sr.building.id = uba.buildingId
//          and sr.user.userId = :userId
//      )
//    order by uba.assignedAt desc
//""")
//    List<UserBuildingAssignmentEntity> findAssignedWithoutAnyResult(@Param("userId") Long userId);
//
//
//    // long countByUser_UserIdAndStatus(Long userId, Integer status);
//
//
//  // @Query("SELECT COUNT(u) FROM UserBuildingAssignmentEntity u")
//  // long countAllAssignments();
//
//  // @Query("SELECT COUNT(u) FROM UserBuildingAssignmentEntity u WHERE u.status = :status")
//  // long countByStatus(@Param("status") int status);
//
//  Optional<UserBuildingAssignmentEntity> findByBuildingId(Long buildingId);
//
//  // 경합 상황 방어 (선택): 행 잠금 후 읽기
//  @Lock(LockModeType.PESSIMISTIC_WRITE)
//  @Query("select u from UserBuildingAssignmentEntity u where u.buildingId = :buildingId")
//  Optional<UserBuildingAssignmentEntity> findByBuildingIdForUpdate(@Param("buildingId") Long buildingId);
//
//  public interface PendingApprovalRow {
//    Long   getId();
//    String getLotAddress();
//    String getRoadAddress();
//    String getBuildingName();
//
//    // ⬇⬇ 컨트롤러에서 쓰는 이름과 '정확히' 일치해야 함
//    Long   getAssignedUserId();
//    String getAssignedName();
//    String getAssignedUsername();
//
//    Long   getApprovalId();
//  }
//
//  @Query(value = """
//    SELECT
//      b.id                         AS id,
//      b.lot_address                AS lotAddress,
//      b.road_address               AS roadAddress,
//      b.building_name              AS buildingName,
//      uba.user_id                  AS assignedUserId,
//      COALESCE(u.name, u.username) AS assignedName,
//      u.username                   AS assignedUsername,
//      uba.approval_id              AS approvalId
//    FROM user_building_assignment uba
//    JOIN building b       ON b.id = uba.building_id
//    LEFT JOIN user_account u ON u.user_id = uba.user_id
//    WHERE uba.user_id IS NOT NULL
//      AND uba.approval_id IS NULL
//      AND (:emd IS NULL OR b.lot_address LIKE CONCAT('%', :emd, '%'))
//    ORDER BY b.id DESC
//  """, nativeQuery = true)
//  List<PendingApprovalRow> findAssignedWithoutApprover(@Param("emd") String eupMyeonDong);
//
//  @Modifying
//  @Transactional
//  @Query(value = "DELETE FROM user_building_assignment WHERE building_id = :buildingId", nativeQuery = true)
//  int deleteByBuildingId(@Param("buildingId") Long buildingId);
//
//  // 대시보드에서 "배정된 건수" = status ∈ (1,2) 인 거 count
//  @Query("SELECT COUNT(u) FROM UserBuildingAssignmentEntity u WHERE u.status IN :statuses")
//  long countByStatusIn(@Param("statuses") List<Integer> statuses);
//
//  /** 조사원 삭제 - 특정 조사원에게 배정된 모든 UBA 엔터티 조회 **/
//  List<UserBuildingAssignmentEntity> findByUser_UserId(Long userId);
//
//}