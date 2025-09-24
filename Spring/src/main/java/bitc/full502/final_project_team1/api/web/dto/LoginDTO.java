package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.api.app.dto.UserInfo;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDTO {
    private boolean success;
    private String message;
    private String name;
    private String role;
    private UserInfo info;
}
