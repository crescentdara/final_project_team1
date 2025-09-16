package bitc.full502.final_project_team1.web.controller;

import bitc.full502.final_project_team1.web.domain.entity.UserAccount;
import bitc.full502.final_project_team1.web.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.web.dto.UserSimpleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserAccountRepository repo;

    @GetMapping
    public List<UserSimpleDto> list(@RequestParam(required = false) String keyword) {
        List<UserAccount> list = (keyword != null && !keyword.isBlank())
                ? repo.findTop100ByNameContainingOrUsernameContainingOrderByUserId(keyword, keyword)
                : repo.findAll(Sort.by(Sort.Direction.ASC, "userId"));
        return list.stream().map(UserSimpleDto::from).toList();
    }
}
