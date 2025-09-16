package bitc.full502.final_project_team1.api.web.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignRequestDTO {
    private Integer userId;
    private List<Long> buildingIds;
}
