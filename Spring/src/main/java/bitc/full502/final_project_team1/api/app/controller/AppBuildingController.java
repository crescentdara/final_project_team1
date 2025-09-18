package bitc.full502.final_project_team1.api.app.controller;

import bitc.full502.final_project_team1.api.app.dto.AppBuildingDetailDto;
import bitc.full502.final_project_team1.core.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/building")
@RequiredArgsConstructor
public class AppBuildingController {

    private final BuildingService buildingService;

    @GetMapping("/detail")
    public AppBuildingDetailDto getBuildingDetail(@RequestParam Long buildingId) {
        return buildingService.getBuildingDetail(buildingId);
    }
}
