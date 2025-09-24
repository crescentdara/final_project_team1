package bitc.full502.final_project_team1.api.web.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignRequestDTO {
    private Long userId;
    private List<Long> buildingIds;
}
