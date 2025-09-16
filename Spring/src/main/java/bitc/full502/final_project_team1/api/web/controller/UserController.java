package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.api.web.dto.UserDetailDto;
import bitc.full502.final_project_team1.api.web.dto.UserSimpleDto;
import bitc.full502.final_project_team1.core.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserAccountRepository userRepo;
    private final AssignmentService assignmentService;

    /** 조사원 목록/검색: GET /api/users?keyword= */
    @GetMapping("/users")
    public List<UserSimpleDto> users(@RequestParam(required = false) String keyword) {
        var list = (keyword == null || keyword.isBlank())
                ? userRepo.findTop200ByOrderByUserIdAsc()
                : userRepo.findTop200ByNameContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrderByUserIdAsc(keyword, keyword);
        return list.stream().map(UserSimpleDto::from).collect(Collectors.toList()); // JDK8 호환
    }

    /** 상세: GET /api/users/{userId} */
    @GetMapping("/users/{userId}")
    public UserDetailDto userDetail(@PathVariable Integer userId) {
        UserAccountEntity u = userRepo.findById(userId).orElseThrow();
        return UserDetailDto.from(u);
    }

    /** 배정 목록: GET /api/users/{userId}/assignments */
    @GetMapping("/users/{userId}/assignments")
    public List<Map<String, Object>> assignments(@PathVariable Integer userId) {
        return assignmentService.getAssignments(userId);
    }

    /** (관리) 라운드로빈 배정 생성: POST /api/assignments/seed?keyword=강동 */
    @PostMapping("/assignments/seed")
    public Map<String, Object> seed(@RequestParam(defaultValue = "강동") String keyword) {
        int created = assignmentService.assignRegionRoundRobin(keyword);
        return java.util.Collections.singletonMap("created", created);
    }
}
