package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.UserCreateDTO;
import bitc.full502.final_project_team1.api.web.dto.UserDetailDto;
import bitc.full502.final_project_team1.api.web.dto.UserSimpleDto;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.enums.Role;
import bitc.full502.final_project_team1.api.web.dto.UserUpdateDto;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.core.service.AssignmentService;
import bitc.full502.final_project_team1.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
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
    private final UserAccountRepository userRepo;
    private final AssignmentService assignmentService;
    private final BuildingRepository buildingRepository;
    private final UserService userService;

    /** 전체 조회 + 검색 (keyword 파라미터 optional) */
    @GetMapping("/users/search")
    public List<UserSimpleDto> list(@RequestParam(required = false) String keyword) {
        List<UserAccountEntity> users;

        if (keyword != null && !keyword.isBlank()) {
            // 🔍 RESEARCHER만 검색
            users = repo.findByRoleAndNameContainingOrRoleAndUsernameContaining(
                    Role.RESEARCHER, keyword,
                    Role.RESEARCHER, keyword
            );
//        } else {
//            // 📋 전체 조회 (RESEARCHER만)
//            users = repo.findByRole(Role.RESEARCHER);
//            users = userRepo.findByRoleAndNameContainingOrRoleAndUsernameContaining(
//                Role.RESEARCHER, keyword,
//                Role.RESEARCHER, keyword
//            );
        } else {
            // 📋 전체 조회 (RESEARCHER만)
            users = userRepo.findByRole(Role.RESEARCHER);
        }

        return users.stream()
            .map(UserSimpleDto::from)
            .toList();
    }

    /** 전체 사용자 조회 + 검색 옵션 */
    @GetMapping("/users")
    public List<UserSimpleDto> users(
        @RequestParam(defaultValue = "전체") String option,
        @RequestParam(required = false) String keyword
    ) {
        String field = normalize(option);
        String kw = keyword == null ? "" : keyword.trim();

        Pageable top200ById = PageRequest.of(0, 200, Sort.by(Sort.Direction.ASC, "userId"));

        List<UserAccountEntity> rows;

        if (kw.isEmpty()) {
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

        return rows.stream().map(UserSimpleDto::from).collect(Collectors.toList());
    }

    /** 조사원 신규 등록 */
    @PostMapping("/users")
    public ResponseEntity<String> createUser(@RequestBody UserCreateDTO dto) {
        UserAccountEntity user = UserAccountEntity.builder()
                .name(dto.getName())
                .username(dto.getUsername())
                .password(dto.getPassword())            // 추후 BCrypt 해싱 권장
                .empNo(generateEmpNo())                 // 사번 자동 생성
                .role(Role.RESEARCHER)                  // 무조건 조사원
                .status(1)                              // 무조건 활성
                .preferredRegion(dto.getPreferredRegion()) // 선호지역 매핑
                .createdAt(LocalDateTime.now())
                .build();

        userRepo.save(user);
        return ResponseEntity.ok("등록 완료");
    }

    /** ✅ 사번 생성 API (React 버튼에서 호출할 수 있도록 추가) */
    @GetMapping("/users/generate-empno")
    public ResponseEntity<String> generateEmpNoApi() {
        return ResponseEntity.ok(generateEmpNo());
    }

    /** 사번 자동 생성 메서드 */
    private String generateEmpNo() {
        String date = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMM"));
        int randomNum = (int) (Math.random() * 9000) + 1000;
        return "EMP" + date + randomNum;
    }

    /** 단건 상세 */
    @GetMapping("/users/{userId}")
    public UserDetailDto userDetail(@PathVariable Long userId) {
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

    /** 배정 목록 */
    @GetMapping("/users/{userId}/assignments")
    public List<Map<String, Object>> assignments(@PathVariable Long userId) {
        return assignmentService.getAssignments(userId);
    }

    /** (관리) 라운드로빈 배정 생성 */
//    @PostMapping("/assignments/seed")
//    public Map<String, Object> seed(@RequestParam(defaultValue = "강동") String keyword) {
//        int created = assignmentService.assignRegionRoundRobin(keyword);
//        return java.util.Collections.singletonMap("created", created);
//    }

    /** 간단 조사원 리스트 조회 (처음 페이지 로드시 사용) */
    @GetMapping("/users/simple")
    public List<UserSimpleDto> getSimpleUsers() {
        List<UserAccountEntity> users = userRepo.findAllByRoleOrderByUserIdAsc(Role.RESEARCHER);
        return users.stream()
            .map(UserSimpleDto::from)
            .toList();
    }

    /** 페이징 조회 */
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
            result = userRepo.findByRole(Role.RESEARCHER, pageable);
        } else {
            switch (field.toLowerCase()) {
                case "name":
                    result = userRepo.findByRoleAndNameContainingIgnoreCase(Role.RESEARCHER, kw, pageable);
                    break;
                case "username":
                    result = userRepo.findByRoleAndUsernameContainingIgnoreCase(Role.RESEARCHER, kw, pageable);
                    break;
                case "empno":
                    result = userRepo.findByRoleAndEmpNoContainingIgnoreCase(Role.RESEARCHER, kw, pageable);
                    break;
                case "all":
                default:
                    result = userRepo.searchAllFields(Role.RESEARCHER, kw, pageable);
                    break;
            }
        }

        return result.map(UserSimpleDto::from);
    }

    /** 아이디 중복 체크 API */
    @GetMapping("/users/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userRepo.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    /** 선호 지역 리스트 API (읍/면/동까지만 추출) */
    @GetMapping("/users/preferred-regions")
    public List<String> getPreferredRegions(@RequestParam(defaultValue = "김해시") String city) {
        return buildingRepository.findDistinctRegions(city).stream()
                .filter(addr -> addr != null && !addr.isBlank())
                .sorted()
                .toList();
    }

    /** 조사원 상세 페이지 - 조사원 정보 수정 **/
    @PutMapping("/users/{userId}")
    public ResponseEntity<String> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateDto dto
    ) {
        userService.updateUser(userId, dto);
        return ResponseEntity.ok("수정 완료");
    }

    /** 조사원 상세 페이지 - 조사원 정보 삭제 **/
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("삭제 완료");
    }

}
