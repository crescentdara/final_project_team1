package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.core.domain.enums.Role;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/web/api/approver")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ApproverController {

  private final UserAccountRepository userRepo;
  private final BuildingRepository buildingRepo;

  // ✅ 공백/널 정규화
  private static String normOrNull(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }

  @GetMapping("/search")
  public List<Map<String, Object>> search(@RequestParam(defaultValue = "") String keyword) {
    String kw = normOrNull(keyword);
    var users = userRepo.searchApprovers("APPROVER", kw);

    return users.stream()
        .map(u -> {
          Map<String, Object> m = new HashMap<>();
          m.put("userId",   u.getUserId());                 // PK
          m.put("username", u.getUsername());               // 화면 보조
          m.put("name",     (u.getName()==null||u.getName().isBlank())
              ? u.getUsername() : u.getName()); // 주 표시
          m.put("empNo",    u.getEmpNo());                  // 사번 (옵션)
          return m;
        })
        .collect(Collectors.toList());
  }

  
}
