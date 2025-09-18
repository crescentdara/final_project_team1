package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.api.web.dto.UserSimpleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/web/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserAccountRepository repo;

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

}
