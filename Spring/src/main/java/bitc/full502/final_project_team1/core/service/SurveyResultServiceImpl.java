package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.AppSurveyResultRequest;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SurveyResultServiceImpl implements SurveyResultService {

    private final SurveyResultRepository surveyResultRepository; // 단일 사용으로 통일
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

    // SurveyResultServiceImpl.java (기존 saveSurvey 교체)
    @Override
    @Transactional
    public SurveyResultEntity saveSurvey(AppSurveyResultRequest dto) {
        var building = buildingRepository.findById(dto.getBuildingId())
                .orElseThrow(() -> new IllegalArgumentException("건물 ID가 존재하지 않습니다."));
        var user = userAccountRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저 ID가 존재하지 않습니다."));

        // 1) TEMP 요청: (user, building, TEMP) 1건만 유지 → 있으면 갱신, 없으면 생성
        if ("TEMP".equalsIgnoreCase(dto.getStatus())) {
            var entity = surveyResultRepository
                    .findByUser_UserIdAndBuilding_IdAndStatus(user.getUserId(), building.getId(), "TEMP")
                    .orElseGet(() -> {
                        var e = new SurveyResultEntity();
                        e.setUser(user);
                        e.setBuilding(building);
                        e.setStatus("TEMP");
                        // createdAt은 @PrePersist 로, 없으면 여기서 now()
                        return e;
                    });

            copyFromDto(entity, dto);          // 필드 덮어쓰기(아래 헬퍼)
            // updatedAt은 @PreUpdate 로, 없으면 여기서 now()
            return surveyResultRepository.save(entity);
        }

        // 2) SENT 요청: TEMP가 있으면 그 레코드를 "승격", 없으면 새로 생성
        if ("SENT".equalsIgnoreCase(dto.getStatus())) {
            var entity = surveyResultRepository
                    .findByUser_UserIdAndBuilding_IdAndStatus(user.getUserId(), building.getId(), "TEMP")
                    .orElseGet(() -> {
                        var e = new SurveyResultEntity();
                        e.setUser(user);
                        e.setBuilding(building);
                        // createdAt은 @PrePersist 로
                        return e;
                    });

            copyFromDto(entity, dto);
            entity.setStatus("SENT");          // ★ TEMP → SENT 전환
            return surveyResultRepository.save(entity);
        }

        throw new IllegalArgumentException("Unsupported status: " + dto.getStatus());
    }

    // 아래 유틸 추가(같은 클래스 안)
    private void copyFromDto(SurveyResultEntity e, AppSurveyResultRequest d) {
        e.setPossible(d.getPossible());
        e.setAdminUse(d.getAdminUse());
        e.setIdleRate(d.getIdleRate());
        e.setSafety(d.getSafety());
        e.setWall(d.getWall());
        e.setRoof(d.getRoof());
        e.setWindowState(d.getWindowState());
        e.setParking(d.getParking());
        e.setEntrance(d.getEntrance());
        e.setCeiling(d.getCeiling());
        e.setFloor(d.getFloor());
        e.setExtEtc(d.getExtEtc());
        e.setIntEtc(d.getIntEtc());
        // 파일 경로는 컨트롤러에서 handleFiles로 주입된 값만 반영
        if (d.getExtPhoto() != null)     e.setExtPhoto(d.getExtPhoto());
        if (d.getExtEditPhoto() != null) e.setExtEditPhoto(d.getExtEditPhoto());
        if (d.getIntPhoto() != null)     e.setIntPhoto(d.getIntPhoto());
        if (d.getIntEditPhoto() != null) e.setIntEditPhoto(d.getIntEditPhoto());
    }


    @Override
    @Transactional // ← 쓰기 트랜잭션
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

        // 파일이 새로 업로드되면 교체 (null/empty 모두 방어)
        if (extPhoto != null && !extPhoto.isEmpty()) {
            entity.setExtPhoto(fileStorageService.storeFile(extPhoto, "ext"));
        }
        if (extEditPhoto != null && !extEditPhoto.isEmpty()) {
            entity.setExtEditPhoto(fileStorageService.storeFile(extEditPhoto, "ext-edit"));
        }
        if (intPhoto != null && !intPhoto.isEmpty()) {
            entity.setIntPhoto(fileStorageService.storeFile(intPhoto, "int"));
        }
        if (intEditPhoto != null && !intEditPhoto.isEmpty()) {
            entity.setIntEditPhoto(fileStorageService.storeFile(intEditPhoto, "int-edit"));
        }

        return surveyResultRepository.save(entity);
    }

    // ===== 관리자/웹 검색 =====

    @Override
    @Transactional(readOnly = true)
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
            return surveyResultRepository.findAll(pageable); // 완전 무필터
        }
        return surveyResultRepository.search(normStatus, normKw, pageable); // 상태만/상태+키워드
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurveyResultEntity> findTempByUser(Long userId) {
        return surveyResultRepository.findByUser_UserIdAndStatus(userId, "TEMP");
    }

    @Transactional(readOnly = true)
    public SurveyResultEntity findByIdOrThrow(Long id) {
        return surveyResultRepository.findById(id).orElseThrow();
    }

    @Override
    @Transactional // 쓰기 트랜잭션
    public int approveBulk(List<Long> ids) {
        var list = surveyResultRepository.findAllById(ids);
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
        var list = surveyResultRepository.findAllById(ids);
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
    @Transactional(readOnly = true)
    public Page<SurveyResultEntity> pageSample(int size) {
        return surveyResultRepository.findAll(
                PageRequest.of(0, Math.max(1, size), Sort.by(Sort.Direction.DESC, "id"))
        );
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<SurveyResultEntity> findByIdWithUserAndBuilding(Long id) {
        // 레포에 이미 있는 @EntityGraph 메서드 사용
        return surveyResultRepository.findByIdWithUserAndBuilding(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SurveyResultEntity> findLatestByUserAndBuilding(Long userId, Long buildingId) {
        return surveyResultRepository
                .findTopByUser_UserIdAndBuilding_IdOrderByUpdatedAtDescCreatedAtDesc(userId, buildingId);
    }

    // status 별 조회
    @Override
    public Page<SurveyResultEntity> findByUserAndStatus(Long userId, String status, int page, int size) {
        return surveyResultRepository.findByUserAndStatusPage(userId, status, PageRequest.of(page, size));
    }
}
