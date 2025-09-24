// src/main/java/.../api/web/controller/ApproverController.java
package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.app.dto.AssignedBuildingDto;
import bitc.full502.final_project_team1.api.web.dto.AssignRequestDTO;
import bitc.full502.final_project_team1.core.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
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

//  /** 선택 건물 → 지정 결재자에게 배정 */
//  @PostMapping(
//      value = "/assign",
//      consumes = MediaType.APPLICATION_JSON_VALUE,
//      produces = MediaType.APPLICATION_JSON_VALUE
//  )
//  @Transactional
//  public Map<String, Object> assign(@RequestBody AssignRequestDTO req) {
//    Long userId = req.getUserId();
//    List<Long> buildingIds = req.getBuildingIds();
//
//    if (userId == null) throw new IllegalArgumentException("userId는 필수입니다.");
//    if (buildingIds == null || buildingIds.isEmpty())
//      throw new IllegalArgumentException("buildingIds가 비어있습니다.");
//
//    // 중복 제거(프론트에서 여러 번 체크했을 때 보호)
//    List<Long> distinctIds = new ArrayList<>(new LinkedHashSet<>(buildingIds));
//
//    int affected = assignmentService.assignToUser(userId, distinctIds); // ✅ Service 경유
//
//    return Map.of(
//        "assignedCount", affected,
//        "userId", userId,
//        "buildingIds", distinctIds
//    );
//  }
//
//  /** 잘못된 요청을 400으로 내려주기 (프론트에서 깔끔히 처리) */
//  @ResponseStatus(HttpStatus.BAD_REQUEST)
//  @ExceptionHandler(IllegalArgumentException.class)
//  public Map<String, Object> handleBadRequest(IllegalArgumentException e) {
//    return Map.of("error", e.getMessage());
//  }
}
