package bitc.full502.final_project_team1.api.app.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListWithStatusResponse<T> {
    private AppUserSurveyStatusResponse status;
    private PageDto<T> page;
}
