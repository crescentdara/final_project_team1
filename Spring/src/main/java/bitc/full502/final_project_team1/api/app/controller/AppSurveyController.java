package bitc.full502.final_project_team1.api.app.controller;

import bitc.full502.final_project_team1.api.app.dto.AssignedBuildingDto;
import bitc.full502.final_project_team1.core.service.AssignmentService;
import bitc.full502.final_project_team1.core.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class AppSurveyController {

    private final SurveyService appSurveyService;
    private final AssignmentService assignmentService;

    @GetMapping("/assigned")
    public List<AssignedBuildingDto> assigned(
            @RequestParam Long userId
    ) {
        return appSurveyService.assigned(userId);
    }

    @GetMapping("/assigned/nearby")
    public List<AssignedBuildingDto> assignedNearby(
            @RequestParam Long userId,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radiusKm
    ) {
        return appSurveyService.assignedWithin(userId, lat, lng, radiusKm);
    }

//    @PostMapping("/assigned/reject")
//    public ResponseEntity<Void> rejectAssignment(@RequestParam Long buildingId) {
//        assignmentService.rejectAssignment(buildingId);
//        return ResponseEntity.ok().build();
//    }

    @PostMapping("/assigned/reject")
    public ResponseEntity<Void> rejectAssignment(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam Long buildingId) {
        assignmentService.rejectAssignment(userId, buildingId);
        return ResponseEntity.ok().build();
    }



}
