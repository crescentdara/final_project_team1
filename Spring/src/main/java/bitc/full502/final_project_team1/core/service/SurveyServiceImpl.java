package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.*;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.repository.AppAssignmentQueryRepository;
import bitc.full502.final_project_team1.core.domain.repository.ApprovalRepository;
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
    private final ApprovalRepository approvalRepository;

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

    @Override
    public AppUserSurveyStatusResponse getStatus(Integer userId) {
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
            Integer userId, String status, int page, int size
    ) {
        Page<SurveyResultEntity> p = surveyResultRepository.findByUserAndStatusPage(
                userId, status, PageRequest.of(page, size));

        // ① 이번 페이지의 survey_result_id 모으기
        var ids = p.getContent().stream().map(SurveyResultEntity::getId).toList();

        // ② approval에서 각 sr_id별 최신 반려사유 한번에 조회 → Map<srId, reason>
        var latestReasons = approvalRepository.findLatestRejectReasons(ids).stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).longValue(),  // survey_result_id
                        r -> (String) r[1]                 // reject_reason
                ));

        // ③ DTO 변환 시 rejectReason 주입
        var items = p.getContent().stream()
                .map(sr -> toItemWithReason(sr, latestReasons.get(sr.getId())))
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


    private SurveyListItemDto toItemWithReason(SurveyResultEntity s, String latestRejectReason) {
        var b = s.getBuilding();
        String address = (b.getRoadAddress() != null && !b.getRoadAddress().isBlank())
                ? b.getRoadAddress() : b.getLotAddress();

        // REJECTED일 때만 노출하고, 없으면 null(또는 폴백으로 s.getIntEtc())
        String rejectReason = null;
        if ("REJECTED".equalsIgnoreCase(s.getStatus())) {
            rejectReason = (latestRejectReason != null && !latestRejectReason.isBlank())
                    ? latestRejectReason
                    : s.getIntEtc(); // 폴백(선택)
        }

        return SurveyListItemDto.builder()
                .surveyId(s.getId())
                .buildingId(b.getId())
                .address(address)
                .buildingName(b.getBuildingName())
                .status(s.getStatus())
                .updatedAtIso(s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : null)
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



}
