package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.*;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.repository.AppAssignmentQueryRepository;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyServiceImpl implements SurveyService {

    private final AppAssignmentQueryRepository appAssignmentQueryRepository;
    private final SurveyResultRepository surveyResultRepository;

    @Override
    public List<AssignedBuildingDto> assigned(Long userId) {
        List<Object[]> rows = appAssignmentQueryRepository.findAssignedAll(userId);
        return rows.stream()
                .map(this::toDtoNoDistance)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignedBuildingDto> assignedWithin(Long userId, double lat, double lng, double radiusKm) {
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

    @Override
    public AppUserSurveyStatusResponse getStatus(Long userId) {
        Map<String, Long> counts = surveyResultRepository.countGroupByStatus(userId).stream()
                .collect(Collectors.toMap(SurveyResultRepository.StatusCount::getStatus,
                        SurveyResultRepository.StatusCount::getCnt));

        long approved = counts.getOrDefault("APPROVED", 0L);
        long rejected = counts.getOrDefault("REJECTED", 0L);
        long sent     = counts.getOrDefault("SENT", 0L);
        long temp     = counts.getOrDefault("TEMP", 0L);

        return new AppUserSurveyStatusResponse(approved, rejected, sent, temp);
    }

    @Override
    public ListWithStatusResponse<SurveyListItemDto> getListWithStatus(
            Long userId, String status, int page, int size
    ) {
        Page<SurveyResultEntity> p = surveyResultRepository.findByUserAndStatusPage(
                userId, status, PageRequest.of(page, size));

        var items = p.getContent().stream()
                .map(this::toItem).toList();

        var state = getStatus(userId);

        return ListWithStatusResponse.<SurveyListItemDto>builder()
                .status(state)
                .page(PageDto.<SurveyListItemDto>builder()
                        .content(items)
                        .number(p.getNumber())
                        .size(p.getSize())
                        .totalElements(p.getTotalElements())
                        .totalPages(p.getTotalPages())
                        .last(p.isLast())
                        .build())
                .build();
    }

    private SurveyListItemDto toItem(SurveyResultEntity s) {
        var b = s.getBuilding();
        String address = (b.getRoadAddress() != null && !b.getRoadAddress().isBlank())
                ? b.getRoadAddress() : b.getLotAddress();

        return SurveyListItemDto.builder()
                .surveyId(s.getId())
                .buildingId(b.getId())
                .address(address)
                .buildingName(b.getBuildingName())
                .status(s.getStatus())
                .updatedAtIso(s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : null)
                // 반려 사유 전용 컬럼이 있으면 교체
                .rejectReason(s.getIntEtc())
                .build();
    }

}
