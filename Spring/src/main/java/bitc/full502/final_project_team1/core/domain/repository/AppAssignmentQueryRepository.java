package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppAssignmentQueryRepository extends JpaRepository<BuildingEntity, Long> {

    @Query(value = """
     SELECT 
       b.id,
       b.lot_address,
       b.latitude,
       b.longitude,
       (6371000 * ACOS(LEAST(1, GREATEST(-1,
         COS(RADIANS(:lat)) * COS(RADIANS(b.latitude)) * COS(RADIANS(b.longitude) - RADIANS(:lng))
         + SIN(RADIANS(:lat)) * SIN(RADIANS(b.latitude))
       )))) AS distance_m,
       a.assigned_at
     FROM java502_team1_final_db.user_building_assignment a
     JOIN java502_team1_final_db.building b 
          ON b.id = a.building_id
     LEFT JOIN java502_team1_final_db.survey_result sr
          ON sr.building_id = a.building_id
         AND sr.user_id     = a.user_id
     WHERE a.user_id = :userId
       AND b.latitude  IS NOT NULL
       AND b.longitude IS NOT NULL
       AND sr.id IS NULL                   -- ✅ 조사결과가 전혀 없는 것만
     HAVING distance_m <= :radiusMeters
     ORDER BY distance_m ASC
     """, nativeQuery = true)
//        SELECT
//          b.id,
//          b.lot_address,
//          b.latitude,
//          b.longitude,
//          (6371000 * ACOS(LEAST(1, GREATEST(-1,
//            COS(RADIANS(:lat)) * COS(RADIANS(b.latitude)) * COS(RADIANS(b.longitude) - RADIANS(:lng))
//            + SIN(RADIANS(:lat)) * SIN(RADIANS(b.latitude))
//          )))) AS distance_m,
//          a.assigned_at                                       -- ★ 추가
//        FROM user_building_assignment a
//        JOIN building b ON b.id = a.building_id
//        WHERE a.user_id = :userId
//          AND b.latitude  IS NOT NULL                         -- ★ null 좌표 방지
//          AND b.longitude IS NOT NULL
//        HAVING distance_m <= :radiusMeters
//        ORDER BY distance_m ASC
//        """, nativeQuery = true)
    List<Object[]> findAssignedWithin(@Param("userId") Long userId,
                                      @Param("lat") double lat,
                                      @Param("lng") double lng,
                                      @Param("radiusMeters") double radiusMeters);


    @Query("""
    select b.id, b.lotAddress, b.latitude, b.longitude, a.assignedAt
    from UserBuildingAssignmentEntity a
    join a.building b
    where a.user.userId = :userId
      and not exists (
          select 1
          from SurveyResultEntity sr
          where sr.user.userId = :userId
            and sr.building.id = b.id
      )
    order by a.assignedAt desc
    """)
    List<Object[]> findAssignedAll(@Param("userId") Long userId);

    @Query(value = """
        SELECT b.id,
               t.max_assigned_at,
               b.latitude,
               b.longitude
        FROM (
            SELECT uba.building_id,
                   MAX(uba.assigned_at) AS max_assigned_at
            FROM user_building_assignment uba
            WHERE uba.user_id = :userId
              AND uba.building_id IN (:buildingIds)
            GROUP BY uba.building_id
        ) t
        JOIN building b ON b.id = t.building_id
        """, nativeQuery = true)
    List<Object[]> findAssignedAtForBuildings(
            @Param("userId") Long userId,
            @Param("buildingIds") List<Long> buildingIds
    );
    //     select b.id, b.lotAddress, b.latitude, b.longitude, a.assignedAt
    //     from UserBuildingAssignmentEntity a
    //     join a.building b
    //     where a.user.userId = :userId
    //     order by a.assignedAt desc
    //     """)
    // List<Object[]> findAssignedAll(@Param("userId") Long userId);
}
