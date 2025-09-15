package bitc.full502.final_project_team1.web.controller;

import bitc.full502.final_project_team1.web.domain.entity.LandSurveyEntity;
import bitc.full502.final_project_team1.web.domain.repository.LandSurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/land-survey")
@RequiredArgsConstructor
public class LandSurveyController {

    private final LandSurveyRepository repository;

    @GetMapping("/regions")
    public List<String> getRegionNames() {
        return repository.findAll()
                .stream()
                .map(LandSurveyEntity::getRegionName)
                .toList();
    }
}
