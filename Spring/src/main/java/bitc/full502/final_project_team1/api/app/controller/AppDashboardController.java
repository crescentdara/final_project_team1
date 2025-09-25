package bitc.full502.final_project_team1.api.app.controller;

import bitc.full502.final_project_team1.api.app.dto.DashboardStatsAppDTO;
import bitc.full502.final_project_team1.core.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class AppDashboardController {

    private final DashboardService dashboardService;

    /**
     * 앱 메인화면 대시보드 통계 API
     * 로그인된 조사자의 활동 현황/조사 현황 리턴
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsAppDTO> getDashboardStats(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-AUTH-TOKEN") String token
    ) {
        DashboardStatsAppDTO stats = dashboardService.getStats(userId);
        return ResponseEntity.ok(stats);
    }
}
