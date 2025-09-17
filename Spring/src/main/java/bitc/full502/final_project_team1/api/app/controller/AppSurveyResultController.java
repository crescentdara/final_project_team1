package bitc.full502.final_project_team1.api.app.controller;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.service.SurveyResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/survey/result")
@RequiredArgsConstructor
public class AppSurveyResultController {

    private final SurveyResultService service;

    @PostMapping
    public ResponseEntity<SurveyResultEntity> create(@RequestBody SurveyResultEntity entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SurveyResultEntity> getOne(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<SurveyResultEntity> getAll() {
        return service.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}