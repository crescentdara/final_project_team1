package bitc.full502.final_project_team1.api.web.dto;

import lombok.*;
import bitc.full502.final_project_team1.core.domain.repository.projection.BuildingListProjection;

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

    private String statusLabel;


    public static BuildingListItemDto from(BuildingListProjection p) {
        boolean assigned = (p.getAssigned() != null && p.getAssigned() == 1);
        String status = p.getResultStatus();

        // 상태 라벨 매핑
        String label;
        if (status != null) {
            switch (status.toUpperCase()) {
                case "TEMP" -> label = "배정";
                case "SENT" -> label = "결재 대기";
                case "APPROVED" -> label = "결재 완료";
                case "REJECTED" -> label = "반려";
                default -> label = assigned ? "배정" : "미배정"; // 모르는 값이면 building.status 참고
            }
        } else {
            // survey_result 자체가 없을 때 → building.status 기준
            label = assigned ? "배정" : "미배정";
        }

        return BuildingListItemDto.builder()
                .buildingId(p.getBuildingId())
                .lotAddress(p.getLotAddress())
                .roadAddress(p.getRoadAddress())
                .assigned(assigned)
                .assignedUserId(p.getAssignedUserId())
                .assignedUserName(p.getAssignedUserName())
                .resultStatus(status)
                .approved("APPROVED".equalsIgnoreCase(status))
                .statusLabel(label)  // 배정/미배정/결재 대기 등등
                .build();
    }


}
