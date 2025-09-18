package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
=======
import lombok.AllArgsConstructor; import lombok.Getter;

@Getter @AllArgsConstructor
>>>>>>> origin/web/his/TotalSurveyList
public class UserSimpleDto {
    private Integer userId;
    private String username;
    private String name;
<<<<<<< HEAD

    public static UserSimpleDto from (UserAccountEntity u) {
        return new UserSimpleDto(u.getUserId(), u.getUsername(), u.getName());
=======
    private String role;     // 문자열로 제공 (EDITOR 등)
    private Integer status;

    public static UserSimpleDto from(UserAccountEntity u) {
        return new UserSimpleDto(
                u.getUserId(),
                u.getUsername(),
                u.getName(),
                u.getRole() != null ? u.getRole().name() : null,
                u.getStatus()
        );
>>>>>>> origin/web/his/TotalSurveyList
    }
}