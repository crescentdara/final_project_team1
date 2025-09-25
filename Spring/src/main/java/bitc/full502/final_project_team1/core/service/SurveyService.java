package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.*;
import jakarta.annotation.Nullable;

import java.util.List;

public interface SurveyService {

    /**
     * 로그인 사용자에게 배정된 모든 조사지(거리 계산 없음).
     */
    List<AssignedBuildingDto> assigned(Long userId);

    /**
     * 중심 좌표와 반경(KM) 내의 배정된 조사지 목록(거리 포함).
     */
    List<AssignedBuildingDto> assignedWithin(Long userId, double lat, double lng, double radiusKm);

    AppUserSurveyStatusResponse getStatus(Long userId);

    ListWithStatusResponse<SurveyListItemDto> getListWithStatus(
            Long userId, String status, int page, int size
    );

    @org.springframework.transaction.annotation.Transactional
    AppSurveyResultResponse saveOrUpdate(AppSurveyResultRequest req, @Nullable String forceStatus);
}
