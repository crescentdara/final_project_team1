package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppAssignmentQueryRepository extends JpaRepository<BuildingEntity, Long> {


    @Query(value = """
            SELECT b.id, b.lot_address, b.latitude, b.longitude, NULL AS distance_m
            FROM user_building_assignment a
            JOIN building b ON b.id = a.building_id
            WHERE a.user_id = :userId
            ORDER BY b.id
            """, nativeQuery = true)
    List<Object[]> findAssignedAll(@Param("userId") Integer userId);

    @Query(value = """
            SELECT 
              b.id,
              b.lot_address,
              b.latitude,
              b.longitude,
              (6371000 * ACOS(LEAST(1, GREATEST(-1,
                COS(RADIANS(:lat)) * COS(RADIANS(b.latitude)) * COS(RADIANS(b.longitude) - RADIANS(:lng))
                + SIN(RADIANS(:lat)) * SIN(RADIANS(b.latitude))
              )))) AS distance_m
            FROM user_building_assignment a
            JOIN building b ON b.id = a.building_id
            WHERE a.user_id = :userId
            HAVING distance_m <= :radiusMeters
            ORDER BY distance_m ASC
            """, nativeQuery = true)
    List<Object[]> findAssignedWithin(@Param("userId") Integer userId,
                                      @Param("lat") double lat,
                                      @Param("lng") double lng,
                                      @Param("radiusMeters") double radiusMeters);
}
