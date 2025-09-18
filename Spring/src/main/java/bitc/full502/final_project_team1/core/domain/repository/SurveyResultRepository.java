package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyResultRepository extends JpaRepository<SurveyResultEntity, Long> {

    List<SurveyResultEntity> findByUser_UserIdAndStatus(Integer userId, String status);

    // 미전송(TEMP) 건수
    long countByUser_UserIdAndStatus(Integer userId, String status);

    // 재조사 대상 (예: safety <= 2 이면 재조사)
    long countByUser_UserIdAndSafetyLessThanEqual(Integer userId, Integer safety);


}

