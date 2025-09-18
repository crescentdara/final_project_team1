package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.AssignedBuildingDto;
import bitc.full502.final_project_team1.core.domain.repository.AppAssignmentQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyServiceImpl implements SurveyService {

    private final AppAssignmentQueryRepository appAssignmentQueryRepository;

    @Override
    public List<AssignedBuildingDto> assigned(Integer userId) {
        List<Object[]> rows = appAssignmentQueryRepository.findAssignedAll(userId);
        return rows.stream()
                .map(this::toDtoNoDistance)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignedBuildingDto> assignedWithin(Integer userId, double lat, double lng, double radiusKm) {
        double meters = radiusKm * 1000.0;
        List<Object[]> rows = appAssignmentQueryRepository.findAssignedWithin(userId, lat, lng, meters);
        return rows.stream()
                .map(this::toDtoWithDistance)
                .collect(Collectors.toList());
    }

    // ────────────────── 내부 변환 유틸 ──────────────────

    private AssignedBuildingDto toDtoNoDistance(Object[] r) {
        return new AssignedBuildingDto(
                toLong(r[0]),   // b.id
                toStr(r[1]),    // b.lotAddress
                toDbl(r[2]),    // b.latitude
                toDbl(r[3]),    // b.longitude
                null,           // distanceMeters
                toDate(r[4])    // ✅ uba.assignedAt
        );
    }

    private AssignedBuildingDto toDtoWithDistance(Object[] r) {
        return new AssignedBuildingDto(
                toLong(r[0]),   // b.id
                toStr(r[1]),    // b.lotAddress
                toDbl(r[2]),    // b.latitude
                toDbl(r[3]),    // b.longitude
                toDbl(r[4]),    // distanceMeters
                toDate(r[5])    // ✅ uba.assignedAt
        );
    }

    private LocalDateTime toDate(Object o) {
        return (o instanceof LocalDateTime dt) ? dt : null;
    }


    private Long toLong(Object o) { return o == null ? null : ((Number) o).longValue(); }
    private Double toDbl(Object o) { return o == null ? null : ((Number) o).doubleValue(); }
    private String toStr(Object o) { return o == null ? null : o.toString(); }

}
