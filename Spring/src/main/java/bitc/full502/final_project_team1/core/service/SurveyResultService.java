package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.web.dto.SurveyResultDTO;
import bitc.full502.final_project_team1.api.web.dto.SurveyResultDetailDto;

import java.util.List;

public interface SurveyResultService {
    // 기존 목록 조회
    List<SurveyResultDTO> getAllSurveyResults();

    SurveyResultDetailDto getSurveyResultDetail(Long id); // 상세 조회
}
