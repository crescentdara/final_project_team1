package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.ApprovalEntity;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import bitc.full502.final_project_team1.api.app.dto.AssignedBuildingDto;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ApprovalRepository extends JpaRepository<ApprovalEntity, Long> {

    /** 특정 빌딩 + 조사원 기준으로 승인/반려 내역 조회 */
    Optional<ApprovalEntity> findByBuildingAndSurveyor(BuildingEntity building, UserAccountEntity surveyor);

    /** 특정 결재자가 승인/반려한 내역 목록 조회 (페이징 지원) */
    Page<ApprovalEntity> findByApprover(UserAccountEntity approver, Pageable pageable);

  // building_id + surveyor_id 유니크 키 기반으로 조회
  Optional<ApprovalEntity> findByBuilding_IdAndSurveyor_UserId(Long buildingId, Long surveyorUserId);

  @EntityGraph(attributePaths = {"building","surveyor","approver","surveyResult"})
  Page<ApprovalEntity> findAll(Pageable pageable);

  // 건물 단위로 "대기중"인 결재가 이미 만들어져 있으면 재사용하고 싶을 때
  @Query("""
        select a from ApprovalEntity a
         where a.building.id = :buildingId
           and a.approvedAt is null
    """)
  Optional<ApprovalEntity> findPendingByBuildingId(Long buildingId);

  @Modifying
  @Transactional
  @Query(value = "DELETE FROM approval WHERE building_id = :buildingId", nativeQuery = true)
  int deleteByBuildingId(@Param("buildingId") Long buildingId);

  /** 조사원 삭제 - 특정 건물과 연관된 approval 삭제 **/
  void deleteByBuilding_Id(Long buildingId);
}
