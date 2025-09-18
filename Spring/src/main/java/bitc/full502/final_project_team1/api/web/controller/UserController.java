package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
<<<<<<< HEAD
import bitc.full502.final_project_team1.api.web.dto.UserSimpleDto;
import bitc.full502.final_project_team1.api.web.dto.UserCreateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/web/api/users")
=======
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
@RequestMapping("/web/api")
>>>>>>> origin/web/his/TotalSurveyList
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

<<<<<<< HEAD

    private final UserAccountRepository repo;

    // 조사원 목록 조회
    @GetMapping
    public List<UserSimpleDto> list(@RequestParam(required = false) String keyword) {
        List<UserAccountEntity> list = (keyword != null && !keyword.isBlank())
                ? repo.findTop100ByNameContainingOrUsernameContainingOrderByUserId(keyword, keyword)
                : repo.findAll(Sort.by(Sort.Direction.ASC, "userId"));
        return list.stream().map(UserSimpleDto::from).toList();
    }

    // 조사원 신규 등록
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserCreateDTO dto) {
        UserAccountEntity user = UserAccountEntity.builder()
                .name(dto.getName())
                .username(dto.getUsername())
                .password(dto.getPassword())            // 추후 BCrypt 해싱 권장
                .empNo(generateEmpNo())                 // 사번 자동 생성
                .role(UserAccountEntity.Role.EDITOR)    // 무조건 조사원
                .status(1)                              // 무조건 활성
                .createdAt(LocalDateTime.now())
                .build();

        repo.save(user);
        return ResponseEntity.ok("등록 완료");
    }

    // ✅ 사번 생성 API (React 버튼에서 호출할 수 있도록 추가)
    @GetMapping("/generate-empno")
    public ResponseEntity<String> generateEmpNoApi() {
        return ResponseEntity.ok(generateEmpNo());
    }

    // 사번 자동 생성 메서드
    private String generateEmpNo() {
        String date = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMM"));
        int randomNum = (int)(Math.random() * 9000) + 1000; // 1000~9999
        return "EMP" + date + randomNum;
    }

=======
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
>>>>>>> origin/web/his/TotalSurveyList
}
