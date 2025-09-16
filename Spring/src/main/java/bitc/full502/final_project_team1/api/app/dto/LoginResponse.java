package bitc.full502.final_project_team1.api.app.dto;

public record LoginResponse(
        boolean success,
        String message,
        String token,
        String name,
        String role,
        UserInfo user
) {}
