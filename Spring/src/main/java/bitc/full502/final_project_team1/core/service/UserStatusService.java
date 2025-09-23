package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.AppUserSurveyStatusResponse;

public interface UserStatusService {
    AppUserSurveyStatusResponse getUserStatus(Long userId);
}