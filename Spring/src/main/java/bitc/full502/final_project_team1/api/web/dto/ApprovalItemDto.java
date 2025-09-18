package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.core.domain.entity.SurveyApprovalEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApprovalItemDto {

    private Long id;
    private String caseNo;
    private String investigator;
    private String address;
    private String priority;
    private String status;
    private String submittedAt;

    public static ApprovalItemDto from (SurveyApprovalEntity e) {
        return new ApprovalItemDto(
                e.getId(),
                e.getCaseNo(),
                e.getInvestigator(),
                e.getAddress(),
                e.getPriority().name(),
                e.getStatus().name(),
                e.getSubmittedAt().toString()
        );
    }
}
