package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.core.domain.entity.ReportEntity;
import bitc.full502.final_project_team1.core.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/web/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** 📌 전체 보고서 조회 */
    @GetMapping
    public List<ReportEntity> getAllReports() {
        return reportService.getAllReports();
    }

    /** 📌 조사원별 보고서 조회 */
    @GetMapping("/user/{userId}")
    public List<ReportEntity> getReportsByUser(@PathVariable Long userId) {
        return reportService.getReportsByUser(userId);
    }

    /** 📌 결재자별 보고서 조회 */
    @GetMapping("/approver/{approverId}")
    public List<ReportEntity> getReportsByApprover(@PathVariable Long approverId) {
        return reportService.getReportsByApprover(approverId);
    }

    /** 📌 건물별 보고서 조회 */
    @GetMapping("/building/{buildingId}")
    public List<ReportEntity> getReportsByBuilding(@PathVariable Long buildingId) {
        return reportService.getReportsByBuilding(buildingId);
    }
}
