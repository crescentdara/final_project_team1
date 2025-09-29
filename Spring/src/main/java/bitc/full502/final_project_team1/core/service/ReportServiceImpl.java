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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepo;
    private final SurveyResultRepository surveyResultRepo;

    // ✅ PdfGenerator 주입 (이제 static 호출 대신 인스턴스 사용)
    private final PdfGenerator pdfGenerator;

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Override
    @Transactional
    public ReportEntity createReport(Long surveyResultId, UserAccountEntity approvedBy) {
        SurveyResultEntity surveyResult = surveyResultRepo.findByIdWithUserAndBuilding(surveyResultId)
            .orElseThrow(() -> new IllegalArgumentException("조사 결과를 찾을 수 없습니다. id=" + surveyResultId));

        // ✅ PdfGenerator 인스턴스 메서드 사용
        String pdfPath = pdfGenerator.generateSurveyReport(
            ResultDetailDto.from(surveyResult),
            approvedBy,
            clientId,
            clientSecret
        );

        System.out.println("📌 Naver ClientId=" + clientId);
        System.out.println("📌 Naver ClientSecret=" + clientSecret);

        ReportEntity report = ReportEntity.builder()
            .surveyResult(surveyResult)
            .approvedBy(approvedBy)
            .approvedAt(LocalDateTime.now())
            .pdfPath(pdfPath)
            .createdAt(LocalDateTime.now())
            .build();

        return reportRepo.save(report);
    }

    /** 📌 전체 보고서 조회 */
    @Override
    public List<ReportEntity> getAllReports() {
        return reportRepo.findAll();
    }

    @Override
    public Optional<ReportEntity> getReportById(Long id) {
        return reportRepo.findById(id);
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
        return reportRepo.findBySurveyResult_Building_Id(buildingId);
    }

    /** 📌 검색 + 정렬 + 페이징 */
    @Override
    public Page<ReportEntity> searchReports(String keyword, String sort, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? "" : keyword;

        // 정렬 처리
        Sort sortOption = Sort.by("createdAt").descending();
        if ("oldest".equalsIgnoreCase(sort)) {
            sortOption = Sort.by("createdAt").ascending();
        }

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

        return reportRepo.searchReports(kw, sortedPageable);
    }
}
