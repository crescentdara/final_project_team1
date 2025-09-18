package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.core.domain.entity.ReportEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;

import java.util.List;

public interface ReportService {

    /** 📌 보고서 생성 (조사 결과 기반 → PDF 생성 & 저장) */
    ReportEntity createReport(Long surveyResultId, UserAccountEntity approvedBy);

    /** 📌 전체 보고서 조회 */
    List<ReportEntity> getAllReports();

    /** 📌 조사원별 보고서 조회 */
    List<ReportEntity> getReportsByUser(Long userId);

    /** 📌 결재자별 보고서 조회 */
    List<ReportEntity> getReportsByApprover(Long approverId);

    /** 📌 건물별 보고서 조회 */
    List<ReportEntity> getReportsByBuilding(Long buildingId);
}
