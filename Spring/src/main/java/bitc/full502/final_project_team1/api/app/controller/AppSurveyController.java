package bitc.full502.final_project_team1.api.app.controller;

import bitc.full502.final_project_team1.api.app.dto.AssignedBuildingDto;
import bitc.full502.final_project_team1.core.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class AppSurveyController {

    private final SurveyService appSurveyService;

    // ex) GET /api/mobile/surveys/assigned?userId=7
    @GetMapping("/assigned")
    public List<AssignedBuildingDto> assigned(
            @RequestParam Long userId
            // TODO: 인증 연동 시 → userId를 토큰/세션에서 읽도록 변경 (기존 웹 미변경)
    ) {
        return appSurveyService.assigned(userId);
    }

    // ex) GET /api/mobile/surveys/assigned/nearby?userId=7&lat=37.5&lng=127.0&radiusKm=2
    @GetMapping("/assigned/nearby")
    public List<AssignedBuildingDto> assignedNearby(
            @RequestParam Long userId,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radiusKm
    ) {
        return appSurveyService.assignedWithin(userId, lat, lng, radiusKm);
    }
}
