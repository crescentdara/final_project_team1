package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.api.web.dto.BuildingSurveyRowDto;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.repository.projection.BuildingListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<BuildingEntity, Long> {

    // 📌 읍면동 중복 없는 리스트 (경상남도 김해시 기준)
    @Query(value = "SELECT DISTINCT SUBSTRING_INDEX(SUBSTRING_INDEX(lot_address, ' ', 3), ' ', -1) " +
            "FROM building " +
            "WHERE lot_address LIKE %:city%", nativeQuery = true)
    List<String> findDistinctEupMyeonDong(@Param("city") String city);

    // 📌 조건 검색 (읍면동 + 미배정 status=0)
    @Query(value = "SELECT * FROM building " +
            "WHERE (:eupMyeonDong IS NULL OR lot_address LIKE %:eupMyeonDong%) " +
            "AND status = 0",
            nativeQuery = true)
    List<BuildingEntity> searchByEupMyeonDong(@Param("eupMyeonDong") String eupMyeonDong);



    // 📌 주소(lotAddress)로 건물 찾기 (위도/경도 조회용)
    Optional<BuildingEntity> findByLotAddress(String lotAddress);

    @Query("select b from BuildingEntity b where b.lotAddress like %:keyword% order by b.id asc")
    List<BuildingEntity> findByLotAddressLike(String keyword);

    // ----------------------------------------------------------------------
    // ★ NEW: 전체 조사 목록(배정/승인 표시 포함) - 네이티브 + 프로젝션 페이징
    //  - filter: 'ALL' | 'UNASSIGNED' | 'ASSIGNED' | 'APPROVED'
    //  - keyword: 번지/도로명/조사원명/아이디 LIKE 검색
    // ----------------------------------------------------------------------
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
    Page<BuildingListProjection> searchBuildings( // ★ NEW
                                                  @Param("keyword") String keyword,
                                                  @Param("filter")  String filter,
                                                  Pageable pageable
    );

<<<<<<< HEAD
=======
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

>>>>>>> origin/app/hsm/ResultDesign
}
