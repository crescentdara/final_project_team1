package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.ApprovalItemDto;
import bitc.full502.final_project_team1.api.web.dto.IdsRequestDto;
import bitc.full502.final_project_team1.api.web.dto.PageResponseDto;
import bitc.full502.final_project_team1.core.domain.entity.SurveyApprovalEntity;
import bitc.full502.final_project_team1.core.domain.entity.SurveyApprovalEntity.Status;
import bitc.full502.final_project_team1.core.domain.repository.SurveyApprovalRepository;
import jdk.jfr.Frequency;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("web/api/approval")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ApprovalController {

    private final SurveyApprovalRepository repo;

    // 목록 (걸재 대기 중 페이지에서 사용)
    // GET /api/approvals?status=PENDING&keyword=&sort=latest&page=1&size=10
    @GetMapping("/web/api/approvals")
    public PageResponseDto<ApprovalItemDto> list(
            @RequestParam(required = false, defaultValue = "PENDING") String status,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean requireKeyword // ★ 추가
    ) {
        // 정렬
        Sort s = "oldest".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Direction.ASC, "submittedAt")
                : Sort.by(Sort.Direction.DESC, "submittedAt");
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size), s);

        // 상태 파싱
        SurveyApprovalEntity.Status st = null;
        if (status != null && !status.trim().isEmpty()) {
            try { st = SurveyApprovalEntity.Status.valueOf(status.trim().toUpperCase()); }
            catch (IllegalArgumentException ignore) {}
        }

        // ★ 검색 모드에서 키워드 공백이면 빈 결과 반환
        String kw = keyword == null ? "" : keyword.trim();
        if (requireKeyword && kw.isEmpty()) {
            return new PageResponseDto<>(java.util.Collections.emptyList(), 0, 0, page, size);
        }

        // 정상 검색
        Page<SurveyApprovalEntity> data = repo.search(st, kw, pageable);
        java.util.List<ApprovalItemDto> rows = data.getContent()
                .stream().map(ApprovalItemDto::from).collect(java.util.stream.Collectors.toList());

        return new PageResponseDto<>(rows, data.getTotalElements(), data.getTotalPages(),
                data.getNumber() + 1, data.getSize());
    }


    // 상세
    @GetMapping("/{id}")
    public ApprovalItemDto detail(@PathVariable Long id) {
        SurveyApprovalEntity e = repo.findById(id).orElseThrow();
        return ApprovalItemDto.from(e);
    }

    // 일괄 승인
    @PatchMapping("/bulk/approve")
    @Transactional
    public Map<String, Object> approve(@RequestBody IdsRequestDto req) {
        List<SurveyApprovalEntity> list = repo.findAllById(req.getIds());
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (SurveyApprovalEntity e : list) {
            if (e.getStatus() != Status.APPROVED) {
                e.setStatus(Status.APPROVED);
                e.setUpdatedAt(now);
                count++;
            }
        }

        return Map.of("updated", count);
    }

    // 일괄 반려
    @PatchMapping("/bulk/reject")
    @Transactional
    public Map<String, Object> reject(@RequestBody IdsRequestDto req) {
        List<SurveyApprovalEntity> list = repo.findAllById(req.getIds());
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (SurveyApprovalEntity e : list ) {
            if (e.getStatus() != Status.REJECTED) {
                e.setStatus(Status.REJECTED);
                e.setUpdatedAt(now);
                count++;
            }
        }

        return Map.of("updated", count);
    }
}
