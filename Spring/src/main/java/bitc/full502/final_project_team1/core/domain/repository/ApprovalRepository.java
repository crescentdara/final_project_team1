package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.ApprovalEntity;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApprovalRepository extends JpaRepository<ApprovalEntity, Long> {

    /** 특정 빌딩 + 조사원 기준으로 승인/반려 내역 조회 */
    Optional<ApprovalEntity> findByBuildingAndSurveyor(BuildingEntity building, UserAccountEntity surveyor);

    /** 특정 결재자가 승인/반려한 내역 목록 조회 (페이징 지원) */
    Page<ApprovalEntity> findByApprover(UserAccountEntity approver, Pageable pageable);
}
