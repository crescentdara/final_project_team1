package bitc.full502.final_project_team1.core.service;

<<<<<<< HEAD
import bitc.full502.final_project_team1.api.app.dto.AppSurveyResultRequest;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SurveyResultServiceImpl implements SurveyResultService {

    private final SurveyResultRepository surveyResultRepository;
    private final BuildingRepository buildingRepository;
    private final UserAccountRepository userAccountRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public SurveyResultEntity save(SurveyResultEntity surveyResult) {
        return surveyResultRepository.save(surveyResult);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SurveyResultEntity> findById(Long id) {
        return surveyResultRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurveyResultEntity> findAll() {
        return surveyResultRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        surveyResultRepository.deleteById(id);
    }

    @Override
    public SurveyResultEntity saveSurvey(AppSurveyResultRequest dto) {
        BuildingEntity building = buildingRepository.findById(dto.getBuildingId())
                .orElseThrow(() -> new IllegalArgumentException("건물 ID가 존재하지 않습니다."));
        UserAccountEntity user = userAccountRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저 ID가 존재하지 않습니다."));

        SurveyResultEntity entity = SurveyResultEntity.builder()
                .possible(dto.getPossible())
                .adminUse(dto.getAdminUse())
                .idleRate(dto.getIdleRate())
                .safety(dto.getSafety())
                .wall(dto.getWall())
                .roof(dto.getRoof())
                .windowState(dto.getWindowState())
                .parking(dto.getParking())
                .entrance(dto.getEntrance())
                .ceiling(dto.getCeiling())
                .floor(dto.getFloor())
                .extEtc(dto.getExtEtc())
                .intEtc(dto.getIntEtc())
                .extPhoto(dto.getExtPhoto())
                .extEditPhoto(dto.getExtEditPhoto())
                .intPhoto(dto.getIntPhoto())
                .intEditPhoto(dto.getIntEditPhoto())
                .status(dto.getStatus())
                .building(building)
                .user(user)
                .build();

        return surveyResultRepository.save(entity);
    }

    @Override
    public SurveyResultEntity updateSurvey(Long id, AppSurveyResultRequest dto,
                                           MultipartFile extPhoto, MultipartFile extEditPhoto,
                                           MultipartFile intPhoto, MultipartFile intEditPhoto) {

        SurveyResultEntity entity = surveyResultRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 설문 없음"));

        // DTO 값 덮어쓰기
        entity.setPossible(dto.getPossible());
        entity.setAdminUse(dto.getAdminUse());
        entity.setIdleRate(dto.getIdleRate());
        entity.setSafety(dto.getSafety());
        entity.setWall(dto.getWall());
        entity.setRoof(dto.getRoof());
        entity.setWindowState(dto.getWindowState());
        entity.setParking(dto.getParking());
        entity.setEntrance(dto.getEntrance());
        entity.setCeiling(dto.getCeiling());
        entity.setFloor(dto.getFloor());
        entity.setExtEtc(dto.getExtEtc());
        entity.setIntEtc(dto.getIntEtc());
        entity.setStatus(dto.getStatus());

        // 파일이 새로 업로드되면 교체
        if (extPhoto != null) entity.setExtPhoto(fileStorageService.storeFile(extPhoto, "ext"));
        if (extEditPhoto != null) entity.setExtEditPhoto(fileStorageService.storeFile(extEditPhoto, "ext-edit"));
        if (intPhoto != null) entity.setIntPhoto(fileStorageService.storeFile(intPhoto, "int"));
        if (intEditPhoto != null) entity.setIntEditPhoto(fileStorageService.storeFile(intEditPhoto, "int-edit"));

        return surveyResultRepository.save(entity);
=======
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyResultServiceImpl implements SurveyResultService {

    private final SurveyResultRepository repo;

    @Override
    public Page<SurveyResultEntity> search(String status, String rawKw, Pageable pageable) {
        String normStatus = (status == null || status.isBlank()) ? null : status.trim();

        // 유니코드 공백 제거 후 빈값이면 null
        String normKw = null;
        if (rawKw != null) {
            String noWs = rawKw.replaceAll("\\s+", ""); // 모든 공백 제거
            if (!noWs.isEmpty()) {
                normKw = rawKw.trim();
            }
        }

        if (normKw == null && normStatus == null) {
            return repo.findAll(pageable); // 완전 무필터
        }
        return repo.search(normStatus, normKw, pageable); // 상태만/상태+키워드
>>>>>>> origin/web/his/MergedTotalSurveyListSearch
    }


    @Override
<<<<<<< HEAD
    @Transactional(readOnly = true)
    public List<SurveyResultEntity> findTempByUser(Integer userId) {
        return surveyResultRepository.findByUser_UserIdAndStatus(userId, "TEMP");
    }




=======
    public SurveyResultEntity findByIdOrThrow(Long id) {
        return repo.findById(id).orElseThrow();
    }

    @Override
    @Transactional // 쓰기 트랜잭션
    public int approveBulk(List<Long> ids) {
        var list = repo.findAllById(ids);
        int count = 0;
        for (var e : list) {
            if (!"APPROVED".equalsIgnoreCase(e.getStatus())) {
                e.setStatus("APPROVED");
                count++;
            }
        }
        return count;
    }

    @Override
    @Transactional // 쓰기 트랜잭션
    public int rejectBulk(List<Long> ids) {
        var list = repo.findAllById(ids);
        int count = 0;
        for (var e : list) {
            if (!"REJECTED".equalsIgnoreCase(e.getStatus())) {
                e.setStatus("REJECTED");
                count++;
            }
        }
        return count;
    }

    @Override
    public Page<SurveyResultEntity> pageSample(int size) {
        return repo.findAll(
                PageRequest.of(0, Math.max(1, size), Sort.by(Sort.Direction.DESC, "id"))
        );
    }
>>>>>>> origin/web/his/MergedTotalSurveyListSearch
}
