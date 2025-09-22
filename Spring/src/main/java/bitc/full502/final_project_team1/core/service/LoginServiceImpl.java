package bitc.full502.final_project_team1.core.service;

import java.util.Objects;
import java.util.UUID;

import bitc.full502.final_project_team1.api.web.dto.LoginDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bitc.full502.final_project_team1.api.app.dto.LoginRequest;
import bitc.full502.final_project_team1.api.app.dto.LoginResponse;
import bitc.full502.final_project_team1.api.app.dto.UserInfo;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;

@Service
@Transactional(readOnly = true)
public class LoginServiceImpl implements LoginService {

    private final UserAccountRepository userRepo;

    public LoginServiceImpl(UserAccountRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        // username = id, status=1 사용자만 허용
        var opt = userRepo.findByUsernameAndStatus(req.id(), 1);
        if (opt.isEmpty()) {
            return new LoginResponse(false, "존재하지 않는 계정이거나 비활성화됨",
                    null, "", "", null);
        }

        UserAccountEntity u = opt.get();

        // 현재 DB 비밀번호가 평문이라 equals 사용 (추후 BCrypt로 교체 권장)
        if (!Objects.equals(u.getPassword(), req.pw())) {
            return new LoginResponse(false, "비밀번호가 올바르지 않습니다.",
                    null, "", "", null);
        }

        String token = UUID.randomUUID().toString(); // 데모 토큰
        UserInfo info = new UserInfo(
                u.getUserId(),
                u.getUsername(),
                u.getName(),
                u.getRole().name(),
                u.getEmpNo()
        );

        return new LoginResponse(true, "로그인 성공",
                token, u.getName(), u.getRole().name(), info);
    }

    @Override
    public LoginDTO loginWeb(LoginRequest req) {
        var opt = userRepo.findByUsernameAndStatus(req.id(), 1);
        if (opt.isEmpty()) {
            return LoginDTO.builder()
                    .success(false)
                    .message("존재하지 않는 계정이거나 비활성화됨")
                    .build();
        }

        UserAccountEntity u = opt.get();

        if (!Objects.equals(u.getPassword(), req.pw())) {
            return LoginDTO.builder()
                    .success(false)
                    .message("비밀번호가 올바르지 않습니다.")
                    .build();
        }

        UserInfo info = new UserInfo(
                u.getUserId(),
                u.getUsername(),
                u.getName(),
                u.getRole().name(),
                u.getEmpNo()
        );

        return LoginDTO.builder()
                .success(true)
                .message("로그인 성공")
                .name(u.getName())
                .role(u.getRole().name())
                .info(info)
                .build();
    }
}
