package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSimpleDto {
    private Long userId;
    private String username;
    private String name;
    private String empNo;
    private String role;
    private Integer status;
    private String preferredRegion;   // 선호 지역 추가

    public static UserSimpleDto from(UserAccountEntity u) {
        return new UserSimpleDto(
                u.getUserId(),
                u.getUsername(),
                u.getName(),
                u.getEmpNo(),
                u.getRole() != null ? u.getRole().name() : null,
                u.getStatus(),
                u.getPreferredRegion()   // 매핑 추가
        );
    }
}
