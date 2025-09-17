package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.DashboardStatsDTO;
import bitc.full502.final_project_team1.core.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 전체 통계 조회
    @GetMapping("/stats")
    public DashboardStatsDTO getDashboardStats() {
        return dashboardService.getStats();
    }
}
