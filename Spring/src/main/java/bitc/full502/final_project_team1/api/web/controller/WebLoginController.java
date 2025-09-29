package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.app.dto.LoginRequest;
import bitc.full502.final_project_team1.api.web.dto.LoginDTO;
import bitc.full502.final_project_team1.core.service.LoginService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/web/api/auth")
@RequiredArgsConstructor
public class WebLoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public LoginDTO login(@RequestBody LoginRequest req, HttpSession session) {
        LoginDTO res = loginService.loginWeb(req); // 웹 전용 메서드 호출

        if (res.isSuccess()) {
            session.setAttribute("loginUser", res.getInfo());
        }

        return res;
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @GetMapping("/me")
    public Object me(HttpSession session) {
        return session.getAttribute("loginUser");
    }
}
