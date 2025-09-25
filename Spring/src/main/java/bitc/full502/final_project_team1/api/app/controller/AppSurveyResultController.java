package bitc.full502.final_project_team1.api.app.controller;

import bitc.full502.final_project_team1.api.app.dto.AppSurveyResultRequest;
import bitc.full502.final_project_team1.api.app.dto.AppSurveyResultResponse; // ✅ 응답 DTO (프리필/상세용)
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.service.FileStorageService;
import bitc.full502.final_project_team1.core.service.SurveyResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import bitc.full502.final_project_team1.core.service.SurveyService;

import java.util.List;

@RestController
@RequestMapping("/app/survey/result")
@RequiredArgsConstructor
public class AppSurveyResultController {

    private final SurveyResultService surveyResultService;
    private final FileStorageService fileStorageService;
    private final SurveyService surveyService;
    // ─────────────────────────────────────────────────────────────────
    // ① 단건 조회 (권한 체크 + DTO 반환)
    //    - 기존: Entity 반환 → 변경: 응답 DTO로 반환
    //    - 같은 유저만 볼 수 있도록 X-USER-ID로 소유 검사
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<AppSurveyResultResponse> getOne(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long id
    ) {
        return surveyResultService.findByIdWithUserAndBuilding(id)
                .filter(sr -> sr.getUser().getUserId().equals(userId))
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─────────────────────────────────────────────────────────────────
    // ② 프리필용: 같은 유저·같은 건물의 "최근 1건" 조회
    //    - 재조사 시작/임시저장 결과수정에서 폼 자동 채움에 사용
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/latest")
    public ResponseEntity<AppSurveyResultResponse> getLatest(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam Long buildingId
    ) {
        return surveyResultService.findLatestByUserAndBuilding(userId, buildingId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(null)); // 없으면 null 반환(앱에서 신건으로 처리)
    }

    // 전체 조회(관리/디버그 용) - 필요 없으면 지워도 OK
    @GetMapping
    public List<SurveyResultEntity> getAll() {
        return surveyResultService.findAll();
    }

    // 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        surveyResultService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────
    // ③ 제출/임시저장/수정: 멀티파트 사용 → consumes 명시 (중요!)
    // ─────────────────────────────────────────────────────────────────
    @PostMapping(value="/submit", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppSurveyResultResponse> submitSurvey(
            @RequestPart("dto") AppSurveyResultRequest dto,
            @RequestPart(value="extPhoto",     required=false) MultipartFile extPhoto,
            @RequestPart(value="extEditPhoto", required=false) MultipartFile extEditPhoto,
            @RequestPart(value="intPhoto",     required=false) MultipartFile intPhoto,
            @RequestPart(value="intEditPhoto", required=false) MultipartFile intEditPhoto
    ){
        dto.setStatus("SENT");
        handleFiles(dto, extPhoto, extEditPhoto, intPhoto, intEditPhoto);
        var res = surveyService.saveOrUpdate(dto, "SENT"); // ★ SurveyService만
        return ResponseEntity.ok(res);
    }

    @PostMapping(value="/save-temp", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppSurveyResultResponse> saveTemp(
            @RequestPart("dto") AppSurveyResultRequest dto,
            @RequestPart(value="extPhoto",     required=false) MultipartFile extPhoto,
            @RequestPart(value="extEditPhoto", required=false) MultipartFile extEditPhoto,
            @RequestPart(value="intPhoto",     required=false) MultipartFile intPhoto,
            @RequestPart(value="intEditPhoto", required=false) MultipartFile intEditPhoto
    ){
        dto.setStatus("TEMP");
        handleFiles(dto, extPhoto, extEditPhoto, intPhoto, intEditPhoto);
        var res = surveyService.saveOrUpdate(dto, "TEMP"); // ★ SurveyService만
        return ResponseEntity.ok(res);
    }

    @PutMapping(value="/edit/{id}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppSurveyResultResponse> updateSurvey(
            @PathVariable Long id,
            @RequestPart("dto") AppSurveyResultRequest dto,
            @RequestPart(value = "extPhoto", required = false) MultipartFile extPhoto,
            @RequestPart(value = "extEditPhoto", required = false) MultipartFile extEditPhoto,
            @RequestPart(value = "intPhoto", required = false) MultipartFile intPhoto,
            @RequestPart(value = "intEditPhoto", required = false) MultipartFile intEditPhoto
    ) {
        dto.setSurveyId(id);
        handleFiles(dto, extPhoto, extEditPhoto, intPhoto, intEditPhoto);
        var res = surveyService.saveOrUpdate(dto, null); // 상태 강제 안 함(요청/기존값 유지)
        return ResponseEntity.ok(res);
    }

    // 파일 저장 공통
    private void handleFiles(AppSurveyResultRequest dto,
                             MultipartFile extPhoto, MultipartFile extEditPhoto,
                             MultipartFile intPhoto, MultipartFile intEditPhoto) {
        if (extPhoto != null)     dto.setExtPhoto(fileStorageService.storeFile(extPhoto, "ext"));
        if (extEditPhoto != null) dto.setExtEditPhoto(fileStorageService.storeFile(extEditPhoto, "ext-edit"));
        if (intPhoto != null)     dto.setIntPhoto(fileStorageService.storeFile(intPhoto, "int"));
        if (intEditPhoto != null) dto.setIntEditPhoto(fileStorageService.storeFile(intEditPhoto, "int-edit"));
    }

    // Entity → 응답 DTO 변환 (프리필/상세 공용)
    private AppSurveyResultResponse toResponse(SurveyResultEntity s) {
        return AppSurveyResultResponse.builder()
                .surveyId(s.getId())
                .possible(s.getPossible())
                .adminUse(s.getAdminUse())
                .idleRate(s.getIdleRate())
                .safety(s.getSafety())
                .wall(s.getWall())
                .roof(s.getRoof())
                .windowState(s.getWindowState())
                .parking(s.getParking())
                .entrance(s.getEntrance())
                .ceiling(s.getCeiling())
                .floor(s.getFloor())
                .extEtc(s.getExtEtc())
                .intEtc(s.getIntEtc())
                .extPhoto(s.getExtPhoto())
                .extEditPhoto(s.getExtEditPhoto())
                .intPhoto(s.getIntPhoto())
                .intEditPhoto(s.getIntEditPhoto())
                .status(s.getStatus())
                .buildingId(s.getBuilding().getId())
                .buildingAddress(s.getBuilding().getLotAddress())
                .userId(s.getUser().getUserId())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    // 상태(status) 별 조회
    @GetMapping("/list")
    public ResponseEntity<Page<AppSurveyResultResponse>> getList(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam(required = false) String status,   // "APPROVED", "SENT", 없으면 전체
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<SurveyResultEntity> results = surveyResultService
                .findByUserAndStatus(userId, status, page, size);

        return ResponseEntity.ok(results.map(this::toResponse));
    }

}
