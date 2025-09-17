package bitc.full502.final_project_team1.api.app.controller;

import bitc.full502.final_project_team1.api.app.dto.LoginRequest;
import bitc.full502.final_project_team1.api.app.dto.LoginResponse;
import bitc.full502.final_project_team1.core.service.LoginService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app")
public class AppUserController {
    private final LoginService loginService;

    public AppUserController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return loginService.login(req);
    }
}