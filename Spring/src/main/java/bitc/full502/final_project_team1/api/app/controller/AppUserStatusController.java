package bitc.full502.final_project_team1.api.app.controller;

import bitc.full502.final_project_team1.api.app.dto.AppUserSurveyStatusResponse;
import bitc.full502.final_project_team1.api.app.dto.ListWithStatusResponse;
import bitc.full502.final_project_team1.api.app.dto.PageDto;
import bitc.full502.final_project_team1.core.service.SurveyService;
import bitc.full502.final_project_team1.core.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import bitc.full502.final_project_team1.api.app.dto.SurveyListItemDto;

import java.util.Locale;

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

    // AppUserStatusController.java
    @GetMapping
    public ListWithStatusResponse<SurveyListItemDto> list(
//            @RequestHeader(value = "X-USER-ID", required = false) Long xUserId,
//            @RequestHeader(value = "userId",   required = false) Long userIdHeader, // 일부 클라/도구 대응
//            @RequestParam(value = "userId", required = false) Long userIdParam,     // 쿼리 파라미터도 허용
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
//        Long userId = (userIdParam != null) ? userIdParam
//                : (xUserId != null)    ? xUserId
//                : userIdHeader;

        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "userId is required in header (X-USER-ID or userId) or as query param"
            );
        }
        String st = (status == null ? null : status.toUpperCase(java.util.Locale.ROOT));
        return surveyService.getListWithStatus(userId, st, page, size);
    }


    // 필요하면 여분 엔드포인트 삭제 가능. 유지한다면 동일한 헤더 허용 로직 적용.
    @GetMapping("/user")
    public PageDto<SurveyListItemDto> listUser(
            @RequestHeader(value = "X-USER-ID", required = false) Long xUserId,
            @RequestHeader(value = "userId",   required = false) Long userIdHeader,
            @RequestParam(value = "userId", required = false) Long userIdParam,
            @RequestParam(defaultValue = "REJECTED") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = (userIdParam != null) ? userIdParam
                : (xUserId != null)    ? xUserId
                : userIdHeader;
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "userId is required in header (X-USER-ID or userId) or as query param"
            );
        }
        return surveyService.getListWithStatus(userId, status.toUpperCase(java.util.Locale.ROOT), page, size)
                .getPage();
    }




}
