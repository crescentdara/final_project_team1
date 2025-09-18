package bitc.full502.final_project_team1.api.app.controller;

import bitc.full502.final_project_team1.api.app.dto.AppUserSurveyStatsResponse;
import bitc.full502.final_project_team1.core.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/survey/stats")
@RequiredArgsConstructor
public class AppUserStatsController {

    private final UserStatsService userStatsService;

    @GetMapping("/{userId}")
    public ResponseEntity<AppUserSurveyStatsResponse> getUserStats(@PathVariable Integer userId) {
        return ResponseEntity.ok(userStatsService.getUserStats(userId));
    }
}
