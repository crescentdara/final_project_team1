package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyResultRepository extends JpaRepository<SurveyResultEntity, Long> {
}

