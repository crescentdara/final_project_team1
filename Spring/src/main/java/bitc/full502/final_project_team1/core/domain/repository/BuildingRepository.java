package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import bitc.full502.final_project_team1.core.domain.repository.projection.BuildingListProjection;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<BuildingEntity, Long> {

  // 📌 읍면동 중복 없는 리스트 (경상남도 김해시 기준)
  @Query(value = "SELECT DISTINCT SUBSTRING_INDEX(SUBSTRING_INDEX(lot_address, ' ', 3), ' ', -1) " +
      "FROM building " +
      "WHERE lot_address LIKE %:city%", nativeQuery = true)
  List<String> findDistinctEupMyeonDong(@Param("city") String city);

  // 📌 조건 검색 (읍면동 + 미배정 status=0) — 기존 로직 유지(NATIVE)
  @Query(value = "SELECT * FROM building " +
      "WHERE (:eupMyeonDong IS NULL OR lot_address LIKE %:eupMyeonDong%) " +
      "AND status = 0",
      nativeQuery = true)
  List<BuildingEntity> searchByEupMyeonDong(@Param("eupMyeonDong") String eupMyeonDong);

  // 📌 조건 검색 (읍면동 + 미배정 status=1) — 기존 로직 유지(NATIVE)
  @Query(value = "SELECT * FROM building " +
      "WHERE (:eupMyeonDong IS NULL OR lot_address LIKE %:eupMyeonDong%) " +
      "AND status = 1",
      nativeQuery = true)
  List<BuildingEntity> assignedResearcher(@Param("eupMyeonDong") String eupMyeonDong);


  // ✅ NEW: 조건 검색 (읍면동 + 미배정 assignedUser IS NULL) — JPQL
  //  - status 컬럼이 아닌, 실제 배정 관계(assignedUser=null) 기준으로도 조회가 필요할 때 사용
  @Query("""
        select b
          from BuildingEntity b
          left join b.assignedUser u
         where u is null
           and (
                :emd is null or trim(:emd) = '' or
                lower(coalesce(b.lotAddress,  '')) like lower(concat('%', trim(:emd), '%')) or
                lower(coalesce(b.roadAddress, '')) like lower(concat('%', trim(:emd), '%')) or
                lower(coalesce(b.buildingName,'')) like lower(concat('%', trim(:emd), '%'))
           )
         order by b.id desc
    """)
  List<BuildingEntity> findUnassignedByEmd(@Param("emd") String emd);

  // 📌 주소(lotAddress)로 건물 찾기 (위도/경도 조회용) — 기존 유지
  Optional<BuildingEntity> findByLotAddress(String lotAddress);

  @Query("select b from BuildingEntity b where b.lotAddress like %:keyword% order by b.id asc")
  List<BuildingEntity> findByLotAddressLike(@Param("keyword") String keyword);


    /** 결재자 정보가 없어서 결재가 없는 쿼리문 **/
    @Query(value = """
    SELECT
       b.id                           AS buildingId,
       b.lot_address                  AS lotAddress,
       b.road_address                 AS roadAddress,
       CASE WHEN uba.building_id IS NULL THEN 0 ELSE 1 END AS assigned,
       ua.user_id                     AS assignedUserId,
       ua.name                        AS assignedUserName,
       sr_latest.id                   AS resultId,
       sr_latest.status               AS resultStatus
    FROM building b
    LEFT JOIN user_building_assignment uba ON uba.building_id = b.id
    LEFT JOIN user_account ua ON ua.user_id = uba.user_id
    LEFT JOIN (
       /* 건물별 최신 조사결과 1건 */
       SELECT sr1.*
       FROM survey_result sr1
       JOIN (
          SELECT building_id, MAX(id) AS max_id
          FROM survey_result
          GROUP BY building_id
       ) mx ON mx.building_id = sr1.building_id AND mx.max_id = sr1.id
    ) sr_latest ON sr_latest.building_id = b.id
    WHERE
      (:keyword IS NULL OR :keyword = '' OR
         b.lot_address  LIKE CONCAT('%', :keyword, '%') OR
         b.road_address LIKE CONCAT('%', :keyword, '%') OR
         ua.name        LIKE CONCAT('%', :keyword, '%') OR
         ua.username    LIKE CONCAT('%', :keyword, '%')
      )
      AND (
         :filter = 'ALL'
         OR (:filter = 'UNASSIGNED' AND uba.building_id IS NULL)
         OR (:filter = 'ASSIGNED'   AND uba.building_id IS NOT NULL)
         OR (:filter = 'APPROVED'   AND sr_latest.status = 'APPROVED')
      )
    ORDER BY b.id DESC
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM building b
    LEFT JOIN user_building_assignment uba ON uba.building_id = b.id
    LEFT JOIN user_account ua ON ua.user_id = uba.user_id
    LEFT JOIN (
       SELECT sr1.*
       FROM survey_result sr1
       JOIN (
          SELECT building_id, MAX(id) AS max_id
          FROM survey_result
          GROUP BY building_id
       ) mx ON mx.building_id = sr1.building_id AND mx.max_id = sr1.id
    ) sr_latest ON sr_latest.building_id = b.id
    WHERE
      (:keyword IS NULL OR :keyword = '' OR
         b.lot_address  LIKE CONCAT('%', :keyword, '%') OR
         b.road_address LIKE CONCAT('%', :keyword, '%') OR
         ua.name        LIKE CONCAT('%', :keyword, '%') OR
         ua.username    LIKE CONCAT('%', :keyword, '%')
      )
      AND (
         :filter = 'ALL'
         OR (:filter = 'UNASSIGNED' AND uba.building_id IS NULL)
         OR (:filter = 'ASSIGNED'   AND uba.building_id IS NOT NULL)
         OR (:filter = 'APPROVED'   AND sr_latest.status = 'APPROVED')
      )
    """,
            nativeQuery = true)
    Page<BuildingListProjection> searchBuildings(
            @Param("keyword") String keyword,
            @Param("filter")  String filter,
            Pageable pageable
    );



    // 📌 읍/면/동 단위까지만 자르기 (면/읍은 우선적으로 끊음)
    @Query(value = """
    SELECT DISTINCT
           TRIM(
               SUBSTRING(lot_address, 1,
                   CASE
                       WHEN LOCATE('읍', REVERSE(lot_address)) > 0 
                            THEN CHAR_LENGTH(lot_address) - LOCATE('읍', REVERSE(lot_address)) + 1
                       WHEN LOCATE('면', REVERSE(lot_address)) > 0 
                            THEN CHAR_LENGTH(lot_address) - LOCATE('면', REVERSE(lot_address)) + 1
                       WHEN LOCATE('동', REVERSE(lot_address)) > 0 
                            THEN CHAR_LENGTH(lot_address) - LOCATE('동', REVERSE(lot_address)) + 1
                       ELSE CHAR_LENGTH(lot_address)
                   END
               )
           ) AS region
    FROM building
    WHERE (:city IS NULL OR lot_address LIKE CONCAT('%', :city, '%'))
    """, nativeQuery = true)
    List<String> findDistinctRegions(@Param("city") String city);


    // 📌 미배정(status=0) + region 조건 (없으면 전체) - 전체 리스트 반환
    @Query(value = """
    SELECT * FROM building
    WHERE status = 0
      AND (:region IS NULL OR :region = '' OR lot_address LIKE %:region%)
    """, nativeQuery = true)
    List<BuildingEntity> findUnassignedByRegion(@Param("region") String region);

  // ✅ NEW: 좌표 조회/검색용 — 정확 일치(번지/도로명/건물명)
  @Query("""
        select b from BuildingEntity b
         where lower(coalesce(b.lotAddress,  '')) = lower(trim(:q))
            or lower(coalesce(b.roadAddress, '')) = lower(trim(:q))
            or lower(coalesce(b.buildingName,'')) = lower(trim(:q))
         order by b.id asc
    """)
  List<BuildingEntity> findByAddressOrNameExact(@Param("q") String query);

  // ✅ NEW: 좌표 조회/검색용 — 포함 검색(번지/도로명/건물명)
  @Query("""
        select b from BuildingEntity b
         where lower(coalesce(b.lotAddress,  '')) like lower(concat('%', trim(:q), '%'))
            or lower(coalesce(b.roadAddress, '')) like lower(concat('%', trim(:q), '%'))
            or lower(coalesce(b.buildingName,'')) like lower(concat('%', trim(:q), '%'))
         order by b.id asc
    """)
  List<BuildingEntity> findByAddressOrNameLike(@Param("q") String query);


  @Query(value = """
  SELECT b.*
    FROM building b
    JOIN user_building_assignment uba ON uba.building_id = b.id
   WHERE (:emd IS NULL OR b.lot_address LIKE CONCAT('%', :emd, '%'))
     AND uba.user_id IS NOT NULL
     AND uba.approval_id IS NULL
   ORDER BY b.id DESC
""", nativeQuery = true)
  List<BuildingEntity> findPendingApprovalBuildings(@Param("emd") String eupMyeonDong);

    // 전체 건물 수
    long count(); // 전체 building 행 개수

    // 배정된 건물 수 (status = 1)
    long countByStatus(int status);

    // 건물 상태 업데이트 (status 변경)
    @Modifying
    @Query("update BuildingEntity b set b.status = :status where b.id = :buildingId")
    void updateStatus(@Param("buildingId") Long buildingId, @Param("status") int status);


}
