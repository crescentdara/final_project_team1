package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.UserCreateDTO;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.api.web.dto.UserDetailDto;
import bitc.full502.final_project_team1.api.web.dto.UserSimpleDto;
import bitc.full502.final_project_team1.core.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
<<<<<<< HEAD:Spring/src/main/java/bitc/full502/final_project_team1/api/web/controller/WebUserController.java
=======
import org.springframework.http.ResponseEntity;
>>>>>>> origin/web/his/MergedTotalSurveyListSearch:Spring/src/main/java/bitc/full502/final_project_team1/api/web/controller/UserController.java
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/web/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class WebUserController {


    private final UserAccountRepository repo;

//    // 조사원 목록 조회
//    @GetMapping("/users/search")
//    public List<UserSimpleDto> list(@RequestParam(required = false) String keyword) {
//        List<UserAccountEntity> list = (keyword != null && !keyword.isBlank())
//                ? repo.findTop100ByNameContainingOrUsernameContainingOrderByUserId(keyword, keyword)
//                : repo.findAll(Sort.by(Sort.Direction.ASC, "userId"));
//        return list.stream().map(UserSimpleDto::from).toList();
//    }

    // 전체 조회 + 검색 (keyword 파라미터 optional)
    @GetMapping("/users/search")
    public List<UserSimpleDto> list(@RequestParam(required = false) String keyword) {
        List<UserAccountEntity> users;

        if (keyword != null && !keyword.isBlank()) {
            // 🔍 EDITOR만 검색
            users = repo.findByRoleAndNameContainingOrRoleAndUsernameContaining(
                    UserAccountEntity.Role.EDITOR, keyword,
                    UserAccountEntity.Role.EDITOR, keyword
            );
        } else {
            // 📋 전체 조회 (EDITOR만)
            users = repo.findByRole(UserAccountEntity.Role.EDITOR);
        }

        return users.stream()
                .map(UserSimpleDto::from)
                .toList();
    }

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

    // 조사원 신규 등록
    @PostMapping("/users")
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
    @GetMapping("/users/generate-empno")
    public ResponseEntity<String> generateEmpNoApi() {
        return ResponseEntity.ok(generateEmpNo());
    }

    // 사번 자동 생성 메서드
    private String generateEmpNo() {
        String date = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMM"));
        int randomNum = (int)(Math.random() * 9000) + 1000; // 1000~9999
        return "EMP" + date + randomNum;
    }

    private final UserAccountRepository userRepo;
    private final AssignmentService assignmentService;


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

    // 간단 조사원 리스트 조회 (처음 페이지 로드시 사용)
    @GetMapping("/users/simple")
    public List<UserSimpleDto> getSimpleUsers() {
        // EDITOR 조사원만, userId 오름차순
        List<UserAccountEntity> users = userRepo.findAllByRoleOrderByUserIdAsc(UserAccountEntity.Role.EDITOR);

        return users.stream()
                .map(UserSimpleDto::from)
                .toList();
    }

    @GetMapping("/users/page")
    public Page<UserSimpleDto> getPagedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String field,
            @RequestParam(required = false) String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("userId").ascending());
        String kw = (keyword == null) ? "" : keyword.trim();

        Page<UserAccountEntity> result;

        if (kw.isEmpty()) {
            result = userRepo.findByRole(UserAccountEntity.Role.EDITOR, pageable);
        } else {
            switch (field.toLowerCase()) {
                case "name":
                    result = userRepo.findByRoleAndNameContainingIgnoreCase(UserAccountEntity.Role.EDITOR, kw, pageable);
                    break;
                case "username":
                    result = userRepo.findByRoleAndUsernameContainingIgnoreCase(UserAccountEntity.Role.EDITOR, kw, pageable);
                    break;
                case "empno":
                    result = userRepo.findByRoleAndEmpNoContainingIgnoreCase(UserAccountEntity.Role.EDITOR, kw, pageable);
                    break;
                case "all":
                default:
                    result = userRepo.searchAllFields(UserAccountEntity.Role.EDITOR, kw, pageable);
                    break;
            }
        }

        return result.map(UserSimpleDto::from);
    }




}
