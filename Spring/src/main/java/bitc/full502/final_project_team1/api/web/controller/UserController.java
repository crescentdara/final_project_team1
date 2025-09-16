package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.api.web.dto.UserSimpleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/web/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserAccountRepository repo;

    @GetMapping
    public List<UserSimpleDto> list(@RequestParam(required = false) String keyword) {
        List<UserAccountEntity> list = (keyword != null && !keyword.isBlank())
                ? repo.findTop100ByNameContainingOrUsernameContainingOrderByUserId(keyword, keyword)
                : repo.findAll(Sort.by(Sort.Direction.ASC, "userId"));
        return list.stream().map(UserSimpleDto::from).toList();
    }
}
