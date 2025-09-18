package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.web.util.PdfGenerator;
import bitc.full502.final_project_team1.core.domain.entity.ReportEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.repository.ReportRepository;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final SurveyResultRepository surveyResultRepository;
    private final UserAccountRepository userAccountRepository;

    @Override
    public ReportEntity createReport(UserBuildingAssignmentEntity assignment,
                                     SurveyResultEntity surveyResult,
                                     String pdfPath,
                                     UserAccountEntity approvedBy) {
        ReportEntity report = ReportEntity.builder()
                .assignment(assignment)
                .surveyResult(surveyResult)
                .pdfPath(pdfPath)
                .approvedBy(approvedBy)
                .approvedAt(LocalDateTime.now())
                .build();

        return reportRepository.save(report);
    }

    @Override
    public List<ReportEntity> getAllReports() {
        return reportRepository.findAll();
    }

    @Override
    public List<ReportEntity> getReportsByUser(Long userId) {
        return reportRepository.findByAssignment_User_UserId(userId);
    }

    @Override
    public List<ReportEntity> getReportsByApprover(Long approverId) {
        return reportRepository.findByApprovedBy_UserId(approverId);
    }

    @Override
    public List<ReportEntity> getReportsByBuilding(Long buildingId) {
        return reportRepository.findByAssignment_Building_Id(buildingId);
    }

    @Override
    public void approveSurveyResult(Long surveyResultId, Integer userId) {
        // 1️⃣ 조사 결과 조회
        SurveyResultEntity surveyResult = surveyResultRepository.findById(surveyResultId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 조사 결과가 없습니다: " + surveyResultId));

        // 2️⃣ 승인자 조회
        UserAccountEntity approver = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자(승인자)가 없습니다: " + userId));

        // 3️⃣ 이미 보고서 존재하는지 확인
        Optional<ReportEntity> existingReport = reportRepository.findBySurveyResult(surveyResult);
        if (existingReport.isPresent()) {
            throw new IllegalStateException("이미 승인된 보고서입니다.");
        }

        // 4️⃣ PDF 생성
        String pdfPath;
        try {
            pdfPath = PdfGenerator.generateSurveyReport(surveyResult);
        } catch (Exception e) {
            throw new RuntimeException("PDF 생성 중 오류 발생", e);
        }

        // 5️⃣ Report 저장
        ReportEntity report = ReportEntity.builder()
                .surveyResult(surveyResult)
                .approvedBy(approver)
                .approvedAt(LocalDateTime.now())
                .pdfPath(pdfPath)
                .assignment(null) // 지금은 null 처리
                .build();

        reportRepository.save(report);

        System.out.println("✅ 승인 처리 완료: surveyResultId=" + surveyResultId + ", approverId=" + userId);
    }


}

