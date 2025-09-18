package bitc.full502.final_project_team1.api.app.controller;

import bitc.full502.final_project_team1.api.app.dto.AppSurveyResultRequest;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.service.FileStorageService;
import bitc.full502.final_project_team1.core.service.SurveyResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/app/survey/result")
@RequiredArgsConstructor
public class AppSurveyResultController {

    private final SurveyResultService surveyResultService;
    private final FileStorageService fileStorageService;

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<SurveyResultEntity> getOne(@PathVariable Long id) {
        return surveyResultService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 전체 조회
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

    // === 신규 저장 (최종 제출) ===
    @PostMapping("/submit")
    public ResponseEntity<SurveyResultEntity> submitSurvey(
            @RequestPart("dto") AppSurveyResultRequest dto,
            @RequestPart(value = "extPhoto", required = false) MultipartFile extPhoto,
            @RequestPart(value = "extEditPhoto", required = false) MultipartFile extEditPhoto,
            @RequestPart(value = "intPhoto", required = false) MultipartFile intPhoto,
            @RequestPart(value = "intEditPhoto", required = false) MultipartFile intEditPhoto
    ) {
        dto.setStatus("SENT"); // 최종 제출

        handleFiles(dto, extPhoto, extEditPhoto, intPhoto, intEditPhoto);

        SurveyResultEntity saved = surveyResultService.saveSurvey(dto);
        return ResponseEntity.ok(saved);
    }

    // === 임시저장 ===
    @PostMapping("/save-temp")
    public ResponseEntity<SurveyResultEntity> saveTemp(
            @RequestPart("dto") AppSurveyResultRequest dto,
            @RequestPart(value = "extPhoto", required = false) MultipartFile extPhoto,
            @RequestPart(value = "extEditPhoto", required = false) MultipartFile extEditPhoto,
            @RequestPart(value = "intPhoto", required = false) MultipartFile intPhoto,
            @RequestPart(value = "intEditPhoto", required = false) MultipartFile intEditPhoto
    ) {
        dto.setStatus("TEMP");

        handleFiles(dto, extPhoto, extEditPhoto, intPhoto, intEditPhoto);

        SurveyResultEntity saved = surveyResultService.saveSurvey(dto);
        return ResponseEntity.ok(saved);
    }

    // === 수정 (임시저장 or 제출 둘 다 가능) ===
    @PutMapping("/edit/{id}")
    public ResponseEntity<SurveyResultEntity> updateSurvey(
            @PathVariable Long id,
            @RequestPart("dto") AppSurveyResultRequest dto,
            @RequestPart(value = "extPhoto", required = false) MultipartFile extPhoto,
            @RequestPart(value = "extEditPhoto", required = false) MultipartFile extEditPhoto,
            @RequestPart(value = "intPhoto", required = false) MultipartFile intPhoto,
            @RequestPart(value = "intEditPhoto", required = false) MultipartFile intEditPhoto
    ) {
        handleFiles(dto, extPhoto, extEditPhoto, intPhoto, intEditPhoto);

        SurveyResultEntity updated = surveyResultService.updateSurvey(id, dto,
                extPhoto, extEditPhoto, intPhoto, intEditPhoto);
        return ResponseEntity.ok(updated);
    }

    // === 파일 저장 처리 공통 메서드 ===
    private void handleFiles(AppSurveyResultRequest dto,
                             MultipartFile extPhoto, MultipartFile extEditPhoto,
                             MultipartFile intPhoto, MultipartFile intEditPhoto) {
        if (extPhoto != null) dto.setExtPhoto(fileStorageService.storeFile(extPhoto, "ext"));
        if (extEditPhoto != null) dto.setExtEditPhoto(fileStorageService.storeFile(extEditPhoto, "ext-edit"));
        if (intPhoto != null) dto.setIntPhoto(fileStorageService.storeFile(intPhoto, "int"));
        if (intEditPhoto != null) dto.setIntEditPhoto(fileStorageService.storeFile(intEditPhoto, "int-edit"));
    }
}
