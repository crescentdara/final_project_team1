package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.web.dto.SurveyResultDTO;
import bitc.full502.final_project_team1.api.web.dto.SurveyResultDetailDto;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyResultServiceImpl implements SurveyResultService {

    private final SurveyResultRepository surveyResultRepository;

    @Override
    public List<SurveyResultDTO> getAllSurveyResults() {
        List<SurveyResultEntity> results = surveyResultRepository.findAll();
        return results.stream()
                .map(SurveyResultDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public SurveyResultDetailDto getSurveyResultDetail(Long id) {
        SurveyResultEntity entity = surveyResultRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 조사 결과가 없습니다: " + id));

        return SurveyResultDetailDto.fromEntity(entity);
    }
}
