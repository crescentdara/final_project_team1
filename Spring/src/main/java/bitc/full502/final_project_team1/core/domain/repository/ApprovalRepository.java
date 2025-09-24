// src/main/java/bitc/full502/final_project_team1/core/domain/repository/ApprovalRepository.java
package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.api.app.dto.AssignedBuildingDto;
import bitc.full502.final_project_team1.core.domain.entity.ApprovalEntity;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRepository extends JpaRepository<ApprovalEntity, Long> {

  // building_id + surveyor_id 유니크 키 기반으로 조회
  Optional<ApprovalEntity> findByBuilding_IdAndSurveyor_UserId(Long buildingId, Long surveyorUserId);

  @EntityGraph(attributePaths = {"building","surveyor","approver","surveyResult"})
  Page<ApprovalEntity> findAll(Pageable pageable);

//  // 📌 미배정(status=0) + region 조건 (없으면 전체) - 전체 리스트 반환
//  @Query(value = """
//    SELECT * FROM building
//    WHERE status = 1
//      AND (:region IS NULL OR :region = '' OR lot_address LIKE %:region%)
//    """, nativeQuery = true)
//  List<AssignedBuildingDto> findAssignedByRegion(@Param("region") String region);
}
