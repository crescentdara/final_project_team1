package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/web/api/users")
@RequiredArgsConstructor
public class UserFcmController {

    private final UserAccountRepository userAccountRepository;

    /** FCM 토큰 업데이트 */
    @PostMapping("/{userId}/fcm-token")
    public ResponseEntity<String> updateFcmToken(
            @PathVariable Long userId,
            @RequestBody String fcmToken) {

        UserAccountEntity user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        user.setFcmToken(fcmToken);
        userAccountRepository.save(user);

        return ResponseEntity.ok("FCM 토큰 업데이트 완료");
    }
}
