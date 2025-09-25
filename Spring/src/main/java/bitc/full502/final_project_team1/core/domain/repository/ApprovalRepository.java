package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.ApprovalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApprovalRepository extends JpaRepository<ApprovalEntity, Long> {

    Optional<ApprovalEntity>
    findTopBySurveyResult_IdOrderByApprovedAtDescIdDesc(Long surveyResultId);

    // 목록 화면 배치 조회: 각 survey_result_id별 최신(approved_at DESC, id DESC) 1건
    @Query(value = """
        SELECT x.survey_result_id, x.reject_reason
        FROM (
            SELECT a.survey_result_id,
                   a.reject_reason,
                   a.approved_at,
                   a.id,
                   ROW_NUMBER() OVER (PARTITION BY a.survey_result_id
                                      ORDER BY a.approved_at DESC, a.id DESC) AS rn
            FROM approval a
            WHERE a.survey_result_id IN (:srIds)
        ) x
        WHERE x.rn = 1
        """, nativeQuery = true)
    List<Object[]> findLatestRejectReasons(@Param("srIds") List<Long> surveyResultIds);
}
