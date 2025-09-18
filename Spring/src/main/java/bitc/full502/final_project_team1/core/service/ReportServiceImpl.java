package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.core.domain.entity.ReportEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

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
}
