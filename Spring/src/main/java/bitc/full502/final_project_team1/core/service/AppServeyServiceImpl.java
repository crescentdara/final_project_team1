package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.AssignedBuildingDto;
import bitc.full502.final_project_team1.core.domain.repository.AppAssignmentQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppServeyServiceImpl implements AppServeyService {

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
                toLong(r[0]),
                toStr(r[1]),
                toDbl(r[2]),
                toDbl(r[3]),
                null
        );
    }

    private AssignedBuildingDto toDtoWithDistance(Object[] r) {
        return new AssignedBuildingDto(
                toLong(r[0]),
                toStr(r[1]),
                toDbl(r[2]),
                toDbl(r[3]),
                toDbl(r[4])
        );
    }

    private Long toLong(Object o) { return o == null ? null : ((Number) o).longValue(); }
    private Double toDbl(Object o) { return o == null ? null : ((Number) o).doubleValue(); }
    private String toStr(Object o) { return o == null ? null : o.toString(); }

}
