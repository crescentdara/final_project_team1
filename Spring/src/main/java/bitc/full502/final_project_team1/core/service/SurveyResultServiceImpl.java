package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SurveyResultServiceImpl implements SurveyResultService {

    private final SurveyResultRepository repository;

    @Override
    @Transactional
    public SurveyResultEntity save(SurveyResultEntity surveyResult) {
        return repository.save(surveyResult);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SurveyResultEntity> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurveyResultEntity> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
