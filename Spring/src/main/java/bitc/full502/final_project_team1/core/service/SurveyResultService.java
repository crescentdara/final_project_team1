package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.AppSurveyResultRequest;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    List<SurveyResultEntity> findTempByUser(Long userId);

    /** 상태/키워드 검색 (키워드가 공백/빈문자면 전체 검색) */
    Page<SurveyResultEntity> search(String status, String rawKw, Pageable pageable);

    /** 상세 조회 (없으면 NoSuchElementException) */
    SurveyResultEntity findByIdOrThrow(Long id);

    /** 일괄 승인/반려 */
    int approveBulk(List<Long> ids);
    int rejectBulk(List<Long> ids);

    /** 디버그용 첫 페이지 */
    Page<SurveyResultEntity> pageSample(int size);

    Optional<SurveyResultEntity> findByIdWithUserAndBuilding(Long id);

    Optional<SurveyResultEntity> findLatestByUserAndBuilding(Long userId, Long buildingId);

    // status 별 조회
    Page<SurveyResultEntity> findByUserAndStatus(Long userId, String status, int page, int size);
}
