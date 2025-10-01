package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.ReportListDto;
import bitc.full502.final_project_team1.core.domain.entity.ReportEntity;
import bitc.full502.final_project_team1.core.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/web/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Value("${file.report-dir}")
    private String reportDir;

    /** 📌 전체/검색 보고서 조회 */
    @GetMapping
    public Page<ReportListDto> getReports(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // 🔹 정렬 조건
        Sort sortOption = sort.equals("oldest")
                ? Sort.by("createdAt").ascending()
                : Sort.by("createdAt").descending();

        Pageable pageable = PageRequest.of(page, size, sortOption);

        if (keyword == null || keyword.isBlank()) {
            // 전체 보고서 불러오기
            List<ReportEntity> list = reportService.getAllReports();

            // 정렬 직접 적용
            list.sort(sort.equals("oldest")
                    ? Comparator.comparing(ReportEntity::getCreatedAt)
                    : Comparator.comparing(ReportEntity::getCreatedAt).reversed());

            List<ReportListDto> dtoList = list.stream()
                    .map(ReportListDto::fromEntity)
                    .toList();

            // ✅ 페이징 적용 (subList 잘라내기)
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), dtoList.size());
            List<ReportListDto> pageContent = (start < end) ? dtoList.subList(start, end) : List.of();

            return new PageImpl<>(pageContent, pageable, dtoList.size());
        } else {
            // 검색일 경우, JPA에서 Pageable 처리
            Page<ReportEntity> pageResult = reportService.searchReports(keyword, sort, pageable);
            return pageResult.map(ReportListDto::fromEntity);
        }
    }

    /** 📌 단일 보고서 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<ReportListDto> getReport(@PathVariable Long id) {
        return reportService.getReportById(id)
                .map(r -> ResponseEntity.ok(ReportListDto.fromEntity(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** 📌 PDF 다운로드 & 보기 */
    @GetMapping("/pdf/{id}")
    public ResponseEntity<Resource> downloadReportPdf(@PathVariable Long id,
                                                      @RequestParam(defaultValue = "inline") String mode) {
        var report = reportService.getReportById(id)
                .orElseThrow(() -> new IllegalArgumentException("보고서를 찾을 수 없습니다. id=" + id));

//        File file = new File(report.getPdfPath());

        Path filePath = Paths.get(reportDir, report.getPdfPath());
        File file = filePath.toFile();

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String disposition = mode.equals("attachment") ? "attachment" : "inline";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + file.getName() + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
