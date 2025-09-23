package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserDetailDto {
    private Long userId;
    private String username;
    private String name;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
    private String preferredRegion;   // 선호 지역 추가

    public static UserDetailDto from(UserAccountEntity u) {
        return new UserDetailDto(
                u.getUserId(),
                u.getUsername(),
                u.getName(),
                u.getRole() != null ? u.getRole().name() : null,
                u.getStatus(),
                u.getCreatedAt(),
                u.getPreferredRegion()   // 매핑 추가
        );
    }
}
