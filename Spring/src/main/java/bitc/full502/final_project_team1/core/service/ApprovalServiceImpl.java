package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.web.dto.ApprovalRequestDTO;
import bitc.full502.final_project_team1.core.domain.entity.ApprovalEntity;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.repository.ApprovalRepository;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.core.service.ApprovalService;
import bitc.full502.final_project_team1.core.service.ReportService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final UserAccountRepository userAccountRepository;
    private final SurveyResultRepository surveyResultRepository;
    private final ReportService reportService;

    /** ✅ 승인 처리 */
    @Override
    @Transactional
    public ApprovalEntity approve(ApprovalRequestDTO dto) {
        // 결재자 조회
        UserAccountEntity approver = userAccountRepository.findById(dto.getApproverId())
                .orElseThrow(() -> new IllegalArgumentException("결재자 계정을 찾을 수 없습니다."));

        // 조사 결과 조회
        SurveyResultEntity surveyResult = surveyResultRepository.findById(dto.getSurveyResultId())
                .orElseThrow(() -> new IllegalArgumentException("조사결과를 찾을 수 없습니다."));

        // 조사자/빌딩은 SurveyResult 에서 가져오기
        UserAccountEntity surveyor = surveyResult.getUser();
        BuildingEntity building = surveyResult.getBuilding();

        // 기존 승인/반려 내역 조회
        Optional<ApprovalEntity> existingOpt = approvalRepository.findByBuildingAndSurveyor(building, surveyor);

        ApprovalEntity entity = existingOpt.orElse(
                ApprovalEntity.builder()
                        .building(building)
                        .surveyor(surveyor)
                        .surveyResult(surveyResult)
                        .build()
        );

        entity.setApprover(approver);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setRejectReason(null);

        // 조사결과 상태 업데이트
        surveyResult.setStatus("APPROVED");

        // PDF 생성
        reportService.createReport(surveyResult.getId(), approver);

        return approvalRepository.save(entity);
    }

    /** ✅ 반려 처리 */
    @Override
    @Transactional
    public ApprovalEntity reject(ApprovalRequestDTO dto) {
        // 결재자 조회
        UserAccountEntity approver = userAccountRepository.findById(dto.getApproverId())
                .orElseThrow(() -> new IllegalArgumentException("결재자 계정을 찾을 수 없습니다."));

        // 조사 결과 조회
        SurveyResultEntity surveyResult = surveyResultRepository.findById(dto.getSurveyResultId())
                .orElseThrow(() -> new IllegalArgumentException("조사결과를 찾을 수 없습니다."));

        // 조사자/빌딩은 SurveyResult 에서 가져오기
        UserAccountEntity surveyor = surveyResult.getUser();
        BuildingEntity building = surveyResult.getBuilding();

        // 기존 승인/반려 내역 조회
        Optional<ApprovalEntity> existingOpt = approvalRepository.findByBuildingAndSurveyor(building, surveyor);

        ApprovalEntity entity = existingOpt.orElse(
                ApprovalEntity.builder()
                        .building(building)
                        .surveyor(surveyor)
                        .surveyResult(surveyResult)
                        .build()
        );

        entity.setApprover(approver);
        entity.setApprovedAt(null);
        entity.setRejectReason(dto.getRejectReason());

        // 조사결과 상태 업데이트
        surveyResult.setStatus("REJECTED");

        return approvalRepository.save(entity);
    }
}
