package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.ApprovalItemDto;
import bitc.full502.final_project_team1.api.web.dto.IdsRequestDto;
import bitc.full502.final_project_team1.api.web.dto.PageResponseDto;
import bitc.full502.final_project_team1.api.web.dto.ResultDetailDto;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.core.service.ReportService;
import bitc.full502.final_project_team1.core.service.SurveyResultService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/web/api")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class SurveyResultController {

    private final SurveyResultService surveyResultService;
    private final SurveyResultRepository repo;
    private final ReportService reportService;
    private final UserAccountRepository userRepo;

    /** 📌 조사결과 리스트 (결재 대기 상태만 조회) */
    @GetMapping("/approvals")
    public PageResponseDto<ApprovalItemDto> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Sort s = "oldest".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Direction.ASC, "id")
                : Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size), s);

        // 🔹 status 무조건 SENT 로 강제 (결재 대기 건만 조회)
        Page<SurveyResultEntity> data = surveyResultService.search("SENT", keyword, pageable);

        var rows = data.getContent().stream()
                .map(ApprovalItemDto::from)
                .toList();

        return new PageResponseDto<>(
                rows,
                data.getTotalElements(),
                data.getTotalPages(),
                data.getNumber() + 1,
                data.getSize()
        );
    }

    /** 📌 조사결과 상세 */
    @GetMapping("/approvals/{id}")
    public ResultDetailDto detail(@PathVariable Long id) {
        var e = repo.findByIdWithUserAndBuilding(id).orElseThrow();
        return ResultDetailDto.from(e);
    }

    /** 일괄 승인 + PDF 생성 */
    @PatchMapping("/approvals/bulk/approve")
    @Transactional
    public Map<String, Object> approve(@RequestBody IdsRequestDto req) {
        var list = repo.findAllById(req.getIds());
        int count = 0;

        // 🔹 관리자 계정 approver로 지정

//        UserAccountEntity approver = userRepo.findById(9l)   // 관리자 PK

        UserAccountEntity approver = userRepo.findById(9L)   // 관리자 PK
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정을 찾을 수 없습니다."));

        for (var e : list) {
            if (!"APPROVED".equalsIgnoreCase(e.getStatus())) {
                e.setStatus("APPROVED");
                count++;

                // PDF 생성 + ReportEntity 저장
                reportService.createReport(e.getId(), approver);
            }
        }
        return Map.of("updated", count);
    }

    /** 일괄 반려 */
    @PatchMapping("/approvals/bulk/reject")
    @Transactional
    public Map<String, Object> reject(@RequestBody IdsRequestDto req) {
        var list = repo.findAllById(req.getIds());
        int count = 0;
        for (var e : list) {
            if (!"REJECTED".equalsIgnoreCase(e.getStatus())) {
                e.setStatus("REJECTED");
                count++;
            }
        }
        return Map.of("updated", count);
    }

}
