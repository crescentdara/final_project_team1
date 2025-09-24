package bitc.full502.final_project_team1.api.web.dto;

import lombok.Data;

@Data
public class ApprovalRequestDTO {
    private Long surveyResultId;
    private Long buildingId;
    private Long surveyorId;
    private Long approverId;     // 승인/반려 수행자
    private String rejectReason; // 반려 사유 (승인일 경우 null)
}

