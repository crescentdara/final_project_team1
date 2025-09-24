package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.AppUserSurveyStatusResponse;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStatusServiceImpl implements UserStatusService {

    private final SurveyResultRepository surveyResultRepository;

    @Override
    @Transactional(readOnly = true)
    public AppUserSurveyStatusResponse getUserStatus(Long userId) {

        Map<String, Long> counts = surveyResultRepository.countGroupByStatus(userId).stream()
                .collect(Collectors.toMap(
                        SurveyResultRepository.StatusCount::getStatus,
                        SurveyResultRepository.StatusCount::getCnt
                ));

        Long approved = counts.getOrDefault("APPROVED", 0L);
        Long rejected = counts.getOrDefault("REJECTED", 0L);
        Long sent     = counts.getOrDefault("SENT", 0L);
        Long temp     = counts.getOrDefault("TEMP", 0L);

        return new AppUserSurveyStatusResponse(approved, rejected, sent, temp);
    }
}
