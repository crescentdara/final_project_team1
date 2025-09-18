package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    // 조사원별 조회
    List<ReportEntity> findByAssignment_User_UserId(Long userId);

    // 결재자별 조회 (PK 이름 맞춰주기)
    List<ReportEntity> findByApprovedBy_UserId(Long approverId);

    // 건물별 조회
    List<ReportEntity> findByAssignment_Building_Id(Long buildingId);
}


