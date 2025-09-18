package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.core.domain.entity.ReportEntity;
import bitc.full502.final_project_team1.core.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/web/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** 📌 전체 보고서 조회 */
    @GetMapping
    public List<ReportEntity> getAllReports() {
        return reportService.getAllReports();
    }

    /** 📌 단일 보고서 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<ReportEntity> getReport(@PathVariable Long id) {
        return reportService.getAllReports().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 📌 PDF 다운로드 & 보기 */
    @GetMapping("/pdf/{id}")
    public ResponseEntity<Resource> downloadReportPdf(@PathVariable Long id) {
        // ReportEntity 조회
        ReportEntity report = reportService.getAllReports().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 보고서를 찾을 수 없습니다. id=" + id));

        File file = new File(report.getPdfPath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getName() + "\"") // inline → 브라우저에서 보기, attachment → 무조건 다운로드
                .body(resource);
    }
}
