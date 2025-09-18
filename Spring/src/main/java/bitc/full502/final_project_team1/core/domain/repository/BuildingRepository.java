package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
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
}
