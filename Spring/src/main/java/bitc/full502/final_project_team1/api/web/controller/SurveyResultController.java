package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.ApprovalItemDto;
import bitc.full502.final_project_team1.api.web.dto.PageResponseDto;
import bitc.full502.final_project_team1.api.web.dto.ResultDetailDto;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import bitc.full502.final_project_team1.core.service.SurveyResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/web/api")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class SurveyResultController {

    private final SurveyResultService surveyResultService;
    private final SurveyResultRepository repo;

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
}
