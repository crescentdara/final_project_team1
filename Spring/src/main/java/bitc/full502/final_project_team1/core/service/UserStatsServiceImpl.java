package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.AppUserSurveyStatsResponse;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserBuildingAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStatsServiceImpl implements UserStatsService {

    private final UserBuildingAssignmentRepository assignmentRepository;
    private final SurveyResultRepository surveyResultRepository;

    @Override
    @Transactional(readOnly = true)
    public AppUserSurveyStatsResponse getUserStats(Integer userId) {
        long scheduled = assignmentRepository.countByUser_UserIdAndStatus(userId, 1); // 조사예정
        long waiting = assignmentRepository.countByUser_UserIdAndStatus(userId, 2);   // 결재대기
        long rejected = assignmentRepository.countByUser_UserIdAndStatus(userId, 4);  // 반려(재조사대상)
        long tempSaved = surveyResultRepository.countByUser_UserIdAndStatus(userId, "TEMP"); // 미전송

        return new AppUserSurveyStatsResponse(scheduled, waiting, rejected, tempSaved);
    }
}