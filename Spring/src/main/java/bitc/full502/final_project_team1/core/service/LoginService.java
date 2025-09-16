package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.LoginRequest;
import bitc.full502.final_project_team1.api.app.dto.LoginResponse;

public interface LoginService {
    LoginResponse login(LoginRequest req);
}