package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.api.web.dto.UserDetailDto;
import bitc.full502.final_project_team1.api.web.dto.UserSimpleDto;
import bitc.full502.final_project_team1.core.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserAccountRepository userRepo;
    private final AssignmentService assignmentService;

    @GetMapping("/users")
    public List<UserSimpleDto> users(
            @RequestParam(defaultValue = "전체") String option,
            @RequestParam(required = false) String keyword
    ) {
        String field = normalize(option);
        String kw = keyword == null ? "" : keyword.trim();

        // 공통: userId 오름차순, 최대 200건
        Pageable top200ById = PageRequest.of(0, 200, Sort.by(Sort.Direction.ASC, "userId"));

        List<UserAccountEntity> rows;

        if (kw.isEmpty()) {
            // 키워드 없으면 전체 상위 200
            rows = userRepo.findTop200ByOrderByUserIdAsc();
        } else {
            switch (field) {
                case "id":
                    rows = userRepo.searchByIdLike(kw, top200ById);
                    break;
                case "username":
                    rows = userRepo.searchByUsernameLikeIgnoreCase(kw, top200ById);
                    break;
                case "name":
                    rows = userRepo.searchByNameLikeIgnoreCase(kw, top200ById);
                    break;
                case "role":
                    rows = userRepo.searchByRoleLikeIgnoreCase(kw, top200ById);
                    break;
                case "all":
                default:
                    rows = userRepo.searchAllLikeIgnoreCase(kw, top200ById);
                    break;
            }
        }

        return rows.stream().map(UserSimpleDto::from).collect(Collectors.toList()); // JDK8 호환
    }

    /** 단건 상세 */
    @GetMapping("/users/{userId}")
    public UserDetailDto userDetail(@PathVariable Integer userId) {
        UserAccountEntity u = userRepo.findById(userId).orElseThrow();
        return UserDetailDto.from(u);
    }

    /** 옵션 한글/영문 매핑 */
    private String normalize(String option) {
        String v = (option == null ? "" : option.trim()).toLowerCase(Locale.ROOT);
        switch (v) {
            case "전체": case "all":      return "all";
            case "id": case "아이디":      return "id";
            case "username": case "계정":  return "username";
            case "이름": case "name":     return "name";
            case "역할": case "role":     return "role";
            default:                      return "all";
        }
    }


    // 배정 목록: GET /api/users/{userId}/assignments
    @GetMapping("/users/{userId}/assignments")
    public List<Map<String, Object>> assignments(@PathVariable Integer userId) {
        return assignmentService.getAssignments(userId);
    }

    //(관리) 라운드로빈 배정 생성: POST /api/assignments/seed?keyword=강동
    @PostMapping("/assignments/seed")
    public Map<String, Object> seed(@RequestParam(defaultValue = "강동") String keyword) {
        int created = assignmentService.assignRegionRoundRobin(keyword);
        return java.util.Collections.singletonMap("created", created);
    }
}
