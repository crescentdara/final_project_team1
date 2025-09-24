package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.core.domain.repository.projection.BuildingListProjection;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BuildingListItemDto {
    private Long   buildingId;
    private String lotAddress;
    private String roadAddress;

    private boolean assigned;          // 배정 여부
    private Long assignedUserId;
    private String  assignedUserName;

    private String  resultStatus;      // 최신 조사/결재 상태 (e.g. APPROVED/SENT/TEMP/...)
    private boolean approved;          // resultStatus == APPROVED

    public static BuildingListItemDto from(BuildingListProjection p) {
        boolean assigned = (p.getAssigned() != null && p.getAssigned() == 1);
        String status = p.getResultStatus();
        return BuildingListItemDto.builder()
                .buildingId(p.getBuildingId())
                .lotAddress(p.getLotAddress())
                .roadAddress(p.getRoadAddress())
                .assigned(assigned)
                .assignedUserId(p.getAssignedUserId())
                .assignedUserName(p.getAssignedUserName())
                .resultStatus(status)
                .approved("APPROVED".equalsIgnoreCase(status))
                .build();
    }
}
