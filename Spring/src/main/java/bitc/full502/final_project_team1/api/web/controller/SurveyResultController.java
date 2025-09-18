package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.SurveyResultDTO;
import bitc.full502.final_project_team1.api.web.dto.SurveyResultDetailDto;
import bitc.full502.final_project_team1.core.service.SurveyResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/web/api/surveys")
@RequiredArgsConstructor
public class SurveyResultController {

    private final SurveyResultService surveyResultService;

    // 결재 대기 목록 조회 (지금은 모든 survey_result 불러오기)
    @GetMapping
    public List<SurveyResultDTO> getAllSurveys() {
        return surveyResultService.getAllSurveyResults();
    }

    // 상세 조회
    @GetMapping("/{id}")
    public SurveyResultDetailDto getSurveyResultDetail(@PathVariable Long id) {
        return surveyResultService.getSurveyResultDetail(id);
    }
}
