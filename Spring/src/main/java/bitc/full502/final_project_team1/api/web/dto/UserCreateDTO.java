package bitc.full502.final_project_team1.api.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDTO {

    private String name;             // 이름
    private String username;         // 아이디
    private String password;         // 비밀번호
    private String empNo;            // 사번
    private String preferredRegion;  // 선호 지역
}
