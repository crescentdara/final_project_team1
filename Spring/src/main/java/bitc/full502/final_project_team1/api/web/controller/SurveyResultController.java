package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.ApprovalItemDto;
import bitc.full502.final_project_team1.api.web.dto.IdsRequestDto;
import bitc.full502.final_project_team1.api.web.dto.PageResponseDto;
import bitc.full502.final_project_team1.api.web.dto.ResultDetailDto;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.service.SurveyResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/web/api")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class SurveyResultController {

    private final SurveyResultService surveyResultService;

    // 리스트 조회
    @GetMapping("/approvals")
    public PageResponseDto<ApprovalItemDto> list(
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean requireKeyword
    ) {
        Sort s = "oldest".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Direction.ASC, "id")
                : Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size), s);

        // 공백/빈 문자열 입력 시 조회 안되도록
//        String kw = keyword == null ? "" : keyword.trim();
//        if (requireKeyword && kw.isEmpty()) {
//            return new PageResponseDto<>(List.of(), 0, 0, page, size);
//        }

        Page<SurveyResultEntity> data = surveyResultService.search(status, keyword, pageable);

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

    // 상세 모달
    @GetMapping("/approvals/{id}")
    public ResultDetailDto detail(@PathVariable Long id) {
        var e = surveyResultService.findByIdOrThrow(id);
        return ResultDetailDto.from(e);
    }

    // 일괄 승인
    @PatchMapping("/approvals/bulk/approve")
    public Map<String, Object> approve(@RequestBody IdsRequestDto req) {
        int updated = surveyResultService.approveBulk(req.getIds());
        return Map.of("updated", updated);
    }

    // 일괄 반려
    @PatchMapping("/approvals/bulk/reject")
    public Map<String, Object> reject(@RequestBody IdsRequestDto req) {
        int updated = surveyResultService.rejectBulk(req.getIds());
        return Map.of("updated", updated);
    }

}
