package bitc.full502.final_project_team1.api.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserInfo(
        Long id,
        String username,
        String name,
        String role,
        String emp_no
) {}
