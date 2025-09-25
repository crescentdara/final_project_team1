package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.*;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
import bitc.full502.final_project_team1.core.domain.repository.AppAssignmentQueryRepository;
import bitc.full502.final_project_team1.core.domain.repository.ApprovalRepository;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyServiceImpl implements SurveyService {

    private final AppAssignmentQueryRepository appAssignmentQueryRepository;
    private final SurveyResultRepository surveyResultRepository;
    private final ApprovalRepository approvalRepository;
    private final RestClient.Builder builder;

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
        if (o == null) return null;
        if (o instanceof LocalDateTime dt) return dt;
        if (o instanceof Timestamp ts) return ts.toLocalDateTime();              // ✅ 핵심
        if (o instanceof java.util.Date d) return new Timestamp(d.getTime()).toLocalDateTime();
        return null; // 알 수 없는 타입이면 null
    }



    private Long toLong(Object o) { return o == null ? null : ((Number) o).longValue(); }
    private Double toDbl(Object o) { return o == null ? null : ((Number) o).doubleValue(); }
    private String toStr(Object o) { return o == null ? null : o.toString(); }

    @Override
    public AppUserSurveyStatusResponse getStatus(Long userId) {
        Map<String, Long> counts = surveyResultRepository.countGroupByStatus(userId).stream()
                .collect(Collectors.toMap(SurveyResultRepository.StatusCount::getStatus,
                        SurveyResultRepository.StatusCount::getCnt));

        Long approved = counts.getOrDefault("APPROVED", 0L);
        Long rejected = counts.getOrDefault("REJECTED", 0L);
        Long sent     = counts.getOrDefault("SENT", 0L);
        Long temp     = counts.getOrDefault("TEMP", 0L);

        return new AppUserSurveyStatusResponse(approved, rejected, sent, temp);
    }



    private SurveyListItemDto toItemWithReason(SurveyResultEntity s, String latestRejectReason, UserBuildingAssignmentEntity u) {
        var b = s.getBuilding();
        String address = (b.getLotAddress() != null && !b.getLotAddress().isBlank())
                ? b.getLotAddress() : b.getRoadAddress();

        String rejectReason = null;
        if ("REJECTED".equalsIgnoreCase(s.getStatus())) {
            rejectReason = (latestRejectReason != null && !latestRejectReason.isBlank())
                    ? latestRejectReason
                    : s.getIntEtc();
        }

        String assignedAtIso = (u != null && u.getAssignedAt() != null)
                ? u.getAssignedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;

        return SurveyListItemDto.builder()
                .surveyId(s.getId())
                .buildingId(b.getId())
                .address(address)
                .buildingName(b.getBuildingName())
                .status(s.getStatus())
                .assignedAtIso(assignedAtIso)
                .rejectReason(rejectReason)
                .build();
    }

    private AppSurveyResultDetailDto toDetail(SurveyResultEntity s) {
        return AppSurveyResultDetailDto.builder()
                .id(s.getId())
                .possible(s.getPossible())
                .adminUse(s.getAdminUse())
                .idleRate(s.getIdleRate())
                .safety(s.getSafety())
                .wall(s.getWall())
                .roof(s.getRoof())
                .windowState(s.getWindowState())
                .parking(s.getParking())
                .entrance(s.getEntrance())
                .ceiling(s.getCeiling())
                .floor(s.getFloor())
                .extEtc(s.getExtEtc())
                .intEtc(s.getIntEtc())
                .extPhoto(s.getExtPhoto())
                .extEditPhoto(s.getExtEditPhoto())
                .intPhoto(s.getIntPhoto())
                .intEditPhoto(s.getIntEditPhoto())
                .status(s.getStatus())
                .buildingId(s.getBuilding().getId())
                .userId(s.getUser().getUserId())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    @Override
    public ListWithStatusResponse<SurveyListItemDto> getListWithStatus(
            Long userId, String status, int page, int size
    ) {
        Page<SurveyResultEntity> p = surveyResultRepository.findByUserAndStatusPage(
                userId, status, PageRequest.of(page, size));

        var srIds = p.getContent().stream().map(SurveyResultEntity::getId).toList();

        var latestReasons = approvalRepository.findLatestRejectReasons(srIds).stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).longValue(),
                        r -> (String) r[1]
                ));

        var buildingIds = p.getContent().stream()
                .map(sr -> sr.getBuilding().getId())
                .distinct()
                .toList();

        // ✅ (building_id, assigned_at) 만 받는 쿼리 → Map<Long, LocalDateTime>
        var assignedAtMap = appAssignmentQueryRepository
                .findAssignedAtForBuildings(userId, buildingIds).stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).longValue(),   // building_id
                        r -> toDate(r[1])                   // Timestamp → LocalDateTime
                ));

        var items = p.getContent().stream()
                .map(sr -> {
                    var b = sr.getBuilding();

                    String address = (b.getLotAddress() != null && !b.getLotAddress().isBlank())
                            ? b.getLotAddress() : b.getRoadAddress();

                    String latestRejectReason = latestReasons.get(sr.getId());
                    String rejectReason = null;
                    if ("REJECTED".equalsIgnoreCase(sr.getStatus())) {
                        rejectReason = (latestRejectReason != null && !latestRejectReason.isBlank())
                                ? latestRejectReason
                                : sr.getIntEtc();
                    }

                    LocalDateTime assignedAt = assignedAtMap.get(b.getId());   // ✅ 배정일자

                    return SurveyListItemDto.builder()
                            .surveyId(sr.getId())
                            .buildingId(b.getId())
                            .address(address)
                            .buildingName(b.getBuildingName())
                            .status(sr.getStatus())
                            .rejectReason(rejectReason)
                            .assignedAtIso(toIso(assignedAt))     // ✅ ISO 문자열
                            .latitude(b.getLatitude())             // ✅ 좌표는 엔티티에서
                            .longitude(b.getLongitude())
                            .build();
                })
                .toList();

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

    private String toIso(LocalDateTime dt) {
        return dt == null ? null : dt.toString(); // ISO-8601
    }

}
