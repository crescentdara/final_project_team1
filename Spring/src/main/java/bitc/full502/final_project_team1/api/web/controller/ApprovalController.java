// src/main/java/bitc/full502/final_project_team1/api/web/controller/ApprovalController.java
package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.ApprovalItemDto;
import bitc.full502.final_project_team1.api.web.dto.IdsRequestDto;
import bitc.full502.final_project_team1.api.web.dto.PageResponseDto;
import bitc.full502.final_project_team1.api.web.dto.ResultDetailDto;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/web/api") // 프론트: /web/api/approvals ...
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ApprovalController {

    private final SurveyResultRepository repo;

    // src/main/java/.../api/web/controller/ApprovalController.java
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
        var pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size), s);

        String st = status.isBlank() ? null : status.trim();
        String kw = keyword.trim();

        if (requireKeyword && kw.isEmpty()) {
            return new PageResponseDto<>(List.of(), 0, 0, page, size);
        }

        Page<SurveyResultEntity> data;
        if (st == null && kw.isEmpty()) {
            data = repo.findAll(pageable);      // 무필터
        } else {
            data = repo.search(st, kw, pageable); // JPQL (2단계에서 주석 해제 필요)
        }

        var rows = data.getContent().stream()
                .map(ApprovalItemDto::from)
                .toList();

        return new PageResponseDto<>(rows, data.getTotalElements(), data.getTotalPages(),
                data.getNumber() + 1, data.getSize());
    }



    /** 상세(모달): GET /web/api/approvals/{id} */
    @GetMapping("/approvals/{id}")
    public ResultDetailDto detail(@PathVariable Long id) {
        var e = repo.findById(id).orElseThrow();
        return ResultDetailDto.from(e);
    }

    /** 일괄 승인: PATCH /web/api/approvals/bulk/approve  { "ids":[1,2] } */
    @PatchMapping("/approvals/bulk/approve")
    @Transactional
    public Map<String, Object> approve(@RequestBody IdsRequestDto req) {
        var list = repo.findAllById(req.getIds());
        int count = 0;
        for (var e : list) {
            if (!"APPROVED".equalsIgnoreCase(e.getStatus())) {
                e.setStatus("APPROVED");
                count++;
            }
        }
        return Map.of("updated", count);
    }

    /** 일괄 반려: PATCH /web/api/approvals/bulk/reject  { "ids":[3] } */
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

    @GetMapping("/approvals/_ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true, "time", java.time.Instant.now().toString());
    }

    /** 2) 디버그: DB에서 5건만 읽어서 DTO로 내려보기 */
    @GetMapping("/approvals/_debug")
    public Map<String, Object> debug() {
        var page = repo.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"))
        );
        var sample = page.getContent().stream()
                .map(ApprovalItemDto::from)
                .toList();
        return Map.of("total", page.getTotalElements(), "sample", sample);
    }

}
