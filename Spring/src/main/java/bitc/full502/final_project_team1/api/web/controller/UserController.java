package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
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
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {


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

}
