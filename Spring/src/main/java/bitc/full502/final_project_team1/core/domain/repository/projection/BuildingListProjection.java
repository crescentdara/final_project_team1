package bitc.full502.final_project_team1.core.domain.repository.projection;

public interface BuildingListProjection {
    Long   getBuildingId();
    String getLotAddress();
    String getRoadAddress();

    Integer getAssigned();          // 1 or 0 (CASE WHEN …)
    Long getAssignedUserId();
    String  getAssignedUserName();

    Long    getResultId();
    String  getResultStatus();

    // 결재자 이름
    String getApproverName();
}
