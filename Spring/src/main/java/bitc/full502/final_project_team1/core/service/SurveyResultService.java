package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;

import java.util.List;
import java.util.Optional;

public interface SurveyResultService {
    SurveyResultEntity save(SurveyResultEntity surveyResult);
    Optional<SurveyResultEntity> findById(Long id);
    List<SurveyResultEntity> findAll();
    void deleteById(Long id);
}
