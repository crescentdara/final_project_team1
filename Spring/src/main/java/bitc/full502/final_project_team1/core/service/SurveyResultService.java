package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.AppSurveyResultRequest;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface SurveyResultService {
    SurveyResultEntity save(SurveyResultEntity surveyResult);
    Optional<SurveyResultEntity> findById(Long id);
    List<SurveyResultEntity> findAll();
    void deleteById(Long id);

    SurveyResultEntity saveSurvey(AppSurveyResultRequest dto);
    SurveyResultEntity updateSurvey(Long id, AppSurveyResultRequest dto,
                                    MultipartFile extPhoto, MultipartFile extEditPhoto,
                                    MultipartFile intPhoto, MultipartFile intEditPhoto);
    List<SurveyResultEntity> findTempByUser(Integer userId);
}
