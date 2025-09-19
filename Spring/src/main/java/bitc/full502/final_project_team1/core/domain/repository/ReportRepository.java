package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.ReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 건물 ID로 조회
    List<ReportEntity> findBySurveyResult_Building_Id(Long buildingId);


    // 🔹 검색 (관리번호 / 조사원 / 주소)
    @Query("""
        SELECT r FROM ReportEntity r
        JOIN r.surveyResult s
        JOIN s.user u
        JOIN s.building b
        WHERE (:keyword = '' 
            OR CAST(s.id AS string) LIKE %:keyword%
            OR u.name LIKE %:keyword%
            OR u.username LIKE %:keyword%
            OR b.lotAddress LIKE %:keyword%)
    """)
    Page<ReportEntity> searchReports(String keyword, Pageable pageable);
}


