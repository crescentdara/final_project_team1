package bitc.full502.final_project_team1.api.app.dto;

public record AssignedBuildingDto(
        Long id,
        String lotAddress,
        Double latitude,
        Double longitude,
        Double distanceMeters // null 가능
) {}
