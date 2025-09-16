package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSimpleDto {
    private Integer userId;
    private String username;
    private String name;

    public static UserSimpleDto from (UserAccountEntity u) {
        return new UserSimpleDto(u.getUserId(), u.getUsername(), u.getName());
    }
}