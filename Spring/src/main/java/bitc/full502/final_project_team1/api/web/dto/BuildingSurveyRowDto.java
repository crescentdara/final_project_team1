// src/main/java/.../api/web/dto/BuildingSurveyRowDto.java
package bitc.full502.final_project_team1.api.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// React 쪽에서 받는 데이터와 서버쪽 데이터를 동일하게 맞춘 DTO
// 조사목록 전체 조회의 검색 시 필요 데이터
@Getter
@AllArgsConstructor
public class BuildingSurveyRowDto {
    private Long buildingId;
    private String lotAddress;
    private String roadAddress;

    private boolean assigned;          // 배정 여부
    private Long assignedUserId;
    private String assignedUserName;

    private String resultStatus;       // 최신 결과 status (null 가능)
    private boolean approved;          // 최신 결과가 APPROVED 인지
}
