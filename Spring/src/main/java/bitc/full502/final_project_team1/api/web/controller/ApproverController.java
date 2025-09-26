package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.AssignApproverRequestDTO;
import bitc.full502.final_project_team1.api.web.dto.AssignApproverResponseDTO;

import bitc.full502.final_project_team1.core.service.AssignmentService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/web/api/approver")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ApproverController {

    private final AssignmentService assignmentService;

  /* 공백/널 정규화 */
  private static String normOrNull(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }

  /** 결재자 목록(ROLE=APPROVER) 검색 */
  @GetMapping(
      value = "/search",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public List<Map<String, Object>> search(@RequestParam(defaultValue = "") String keyword) {
    var users = assignmentService.searchApprovers(normOrNull(keyword)); // ✅ Service 경유
    return users.stream()
        .map(u -> Map.<String,Object>of(
            "userId",   u.getUserId(),
            "username", u.getUsername(),
            "name",     (u.getName()==null || u.getName().isBlank()) ? u.getUsername() : u.getName(),
            "empNo",    u.getEmpNo()
        ))
        .collect(Collectors.toList());
  }

  @PostMapping(
      value = "/assign",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<AssignApproverResponseDTO> assign(@RequestBody AssignApproverRequestDTO req) {
    AssignApproverResponseDTO res = assignmentService.assignApprover(req);
    return ResponseEntity.ok(res);
  }

}
