package bitc.full502.final_project_team1.api.app.controller;

import bitc.full502.final_project_team1.api.app.dto.AppUserSurveyStatusResponse;
import bitc.full502.final_project_team1.api.app.dto.ListWithStatusResponse;
import bitc.full502.final_project_team1.api.app.dto.SurveyListItemDto;
import bitc.full502.final_project_team1.core.service.SurveyService;
import bitc.full502.final_project_team1.core.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/survey/status")
@RequiredArgsConstructor
public class AppUserStatusController {

    private final UserStatusService userStatusService;

    private final SurveyService surveyService;

    @GetMapping("/{userId}")
    public ResponseEntity<AppUserSurveyStatusResponse> getUserStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(userStatusService.getUserStatus(userId));
    }

    /** 상단 카운트만 */
    @GetMapping("/status")
    public ResponseEntity<AppUserSurveyStatusResponse> stats(
            @RequestHeader("X-USER-ID") Long userId
    ) {
        return ResponseEntity.ok(userStatusService.getUserStatus(userId));
    }

    /** 목록 + 상단 카운트 (status 필터는 선택) */
    @GetMapping
    public ListWithStatusResponse<SurveyListItemDto> list(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return surveyService.getListWithStatus(userId, status, page, size);
    }
}
