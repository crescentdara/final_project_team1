package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BuildingRepository extends JpaRepository<BuildingEntity, Long> {

    @Query("select b from BuildingEntity b where b.lotAddress like %:keyword% order by b.id asc")
    List<BuildingEntity> findByLotAddressLike(String keyword);
}
