package bitc.full502.final_project_team1.web.dto;

import bitc.full502.final_project_team1.web.domain.entity.UserAccount;
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

    public static UserSimpleDto from (UserAccount u) {
        return new UserSimpleDto(u.getUserId(), u.getUsername(), u.getName());
    }
}
