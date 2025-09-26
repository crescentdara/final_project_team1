package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.core.domain.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {
    private String name;
    private String preferredRegion;
    private Role role;
    private String username;
}
