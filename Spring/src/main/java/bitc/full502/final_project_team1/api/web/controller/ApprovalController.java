package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.ApprovalRequestDTO;
import bitc.full502.final_project_team1.core.domain.entity.ApprovalEntity;
import bitc.full502.final_project_team1.core.service.ApprovalService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/web/api/approval")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    /** 단건 승인 + PDF 생성 */
    @PostMapping("/{surveyResultId}/approve")
    @Transactional
    public ResponseEntity<?> approve(@PathVariable Long surveyResultId,
                                     @RequestBody ApprovalRequestDTO dto) {
        dto.setSurveyResultId(surveyResultId); // PathVariable → DTO 매핑
        ApprovalEntity approval = approvalService.approve(dto);

        return ResponseEntity.ok(Map.of(
                "id", approval.getId(),
                "status", "APPROVED"
        ));
    }

    /** 단건 반려 */
    @PostMapping("/{surveyResultId}/reject")
    @Transactional
    public ResponseEntity<?> reject(@PathVariable Long surveyResultId,
                                    @RequestBody ApprovalRequestDTO dto) {
        dto.setSurveyResultId(surveyResultId); // PathVariable → DTO 매핑
        ApprovalEntity approval = approvalService.reject(dto);

        return ResponseEntity.ok(Map.of(
                "id", approval.getId(),
                "status", "REJECTED"
        ));
    }
}

