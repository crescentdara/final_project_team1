package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.core.domain.entity.ReportEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReportService {

    /** 📌 보고서 생성 (조사 결과 기반 → PDF 생성 & 저장) */
    ReportEntity createReport(Long surveyResultId, UserAccountEntity approvedBy);

    /** 📌 전체 보고서 조회 */
    List<ReportEntity> getAllReports();

    /** 📌 단일 보고서 조회 */
    Optional<ReportEntity> getReportById(Long id);

    /** 📌 조사원별 보고서 조회 */
    List<ReportEntity> getReportsByUser(Long userId);

    /** 📌 결재자별 보고서 조회 */
    List<ReportEntity> getReportsByApprover(Long approverId);

    /** 📌 건물별 보고서 조회 */
    List<ReportEntity> getReportsByBuilding(Long buildingId);

    // 🔹 검색 + 정렬 + 페이징 (교체 완료)
    Page<ReportEntity> searchReports(String keyword, String sort, Pageable pageable);
}
