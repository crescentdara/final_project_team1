package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.DashboardStatsAppDTO;
import bitc.full502.final_project_team1.api.web.dto.DashboardStatsDTO;

public interface DashboardService {
    DashboardStatsDTO getStats();                  // 웹용 (전체 기준)
    DashboardStatsAppDTO getStats(Long userId);    // 앱용 (조사자 기준)
}
