package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.web.dto.ResultDetailDto;
import bitc.full502.final_project_team1.api.web.util.PdfGenerator;
import bitc.full502.final_project_team1.core.domain.entity.ReportEntity;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.repository.ReportRepository;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepo;
    private final SurveyResultRepository surveyResultRepo;

    /** 📌 승인 시 보고서 생성 */
    @Override
    @Transactional
    public ReportEntity createReport(Long surveyResultId, UserAccountEntity approvedBy) {
        // 1. 조사 결과 조회
        SurveyResultEntity surveyResult = surveyResultRepo.findById(surveyResultId)
                .orElseThrow(() -> new IllegalArgumentException("조사 결과를 찾을 수 없습니다. id=" + surveyResultId));

        // 2. DTO 변환
        ResultDetailDto dto = ResultDetailDto.from(surveyResult);

        // 3. PDF 생성
        String pdfPath = PdfGenerator.generateSurveyReport(dto, approvedBy);

        // 4. ReportEntity 저장
        ReportEntity report = ReportEntity.builder()
                .surveyResult(surveyResult)
                //.assignment(surveyResult.getAssignment()) // assignment가 nullable일 수도 있음
                .approvedBy(approvedBy)
                .approvedAt(LocalDateTime.now())
                .pdfPath(pdfPath)
                .build();

        return reportRepo.save(report);
    }

    /** 📌 전체 보고서 조회 */
    @Override
    public List<ReportEntity> getAllReports() {
        return reportRepo.findAll();
    }

    /** 📌 조사원별 보고서 조회 */
    @Override
    public List<ReportEntity> getReportsByUser(Long userId) {
        return reportRepo.findByAssignment_User_UserId(userId);
    }

    /** 📌 결재자별 보고서 조회 */
    @Override
    public List<ReportEntity> getReportsByApprover(Long approverId) {
        return reportRepo.findByApprovedBy_UserId(approverId);
    }

    /** 📌 건물별 보고서 조회 */
    @Override
    public List<ReportEntity> getReportsByBuilding(Long buildingId) {
        return reportRepo.findByAssignment_Building_Id(buildingId);
    }
}
