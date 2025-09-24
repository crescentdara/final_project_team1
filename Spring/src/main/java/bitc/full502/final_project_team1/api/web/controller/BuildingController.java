package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.AssignRequestDTO;
import bitc.full502.final_project_team1.api.web.dto.BuildingDTO;
import bitc.full502.final_project_team1.api.web.dto.BuildingListItemDto;
import bitc.full502.final_project_team1.api.web.dto.PageResponseDto;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserBuildingAssignmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/web/building")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingRepository buildingRepo;
    private final UserAccountRepository userRepo;
    private final UserBuildingAssignmentRepository assignmentRepo;

    // 📌 전체 건물 목록 조회
    @GetMapping
    public List<BuildingEntity> getAllBuildings() {
        return buildingRepo.findAll();
    }

    // 📌 특정 ID로 건물 조회
    @GetMapping("/{id}")
    public BuildingEntity getBuildingById(@PathVariable Long id) {
        return buildingRepo.findById(id).orElse(null);
    }

    // 📌 건물명 리스트 조회 (중복 제거 + 빈 값 제외)
    @GetMapping("/names")
    public List<String> getBuildingNames() {
        return buildingRepo.findAll()
                .stream()
                .map(BuildingEntity::getBuildingName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .toList();
    }

    // 📌 lotAddress만 단독으로 조회 (중복 제거 + 빈 값 제외)
    @GetMapping("/addresses")
    public List<String> getBuildingAddresses() {
        return buildingRepo.findAll()
                .stream()
                .map(BuildingEntity::getLotAddress)
                .filter(addr -> addr != null && !addr.isBlank())
                .distinct()
                .toList();
    }

    // 📌 lotAddress + 번-지 (+ 보조정보) 조회 (중복 제거)
    @GetMapping("/address-details")
    public List<String> getBuildingAddressDetails() {
        return buildingRepo.findAll()
                .stream()
                .map(b -> {
                    StringBuilder sb = new StringBuilder();

                    if (b.getLotAddress() != null && !b.getLotAddress().isBlank()) {
                        sb.append(b.getLotAddress());
                    }

                    boolean hasMain = b.getLotMainNo() != null && !b.getLotMainNo().isBlank() && !"0".equals(b.getLotMainNo());
                    boolean hasSub = b.getLotSubNo() != null && !b.getLotSubNo().isBlank() && !"0".equals(b.getLotSubNo());

                    if (hasMain) {
                        int mainNo = Integer.parseInt(b.getLotMainNo());
                        sb.append(" ").append(mainNo);

                        if (hasSub) {
                            int subNo = Integer.parseInt(b.getLotSubNo());
                            sb.append("-").append(subNo);
                        }
                        sb.append("번지");
                    }
                    else {
                        if (b.getRoadAddress() != null && !b.getRoadAddress().isBlank()) {
                            sb.append(" (").append(b.getRoadAddress()).append(")");
                        }
                        else if (b.getBuildingName() != null && !b.getBuildingName().isBlank()) {
                            sb.append(" - ").append(b.getBuildingName());
                        }
                    }

                    return sb.toString();
                })
                .filter(addr -> addr != null && !addr.isBlank())
                .distinct()
                .toList();
    }

    // 📌 읍면동 목록 조회 (경상남도 김해시 기준)
    @GetMapping("/eupmyeondong")
    public List<String> getEupMyeonDong(@RequestParam String city) {
        return buildingRepo.findDistinctEupMyeonDong(city);
    }

    // 📌 읍면동 기준 검색 (미배정만 내려옴)
    @GetMapping("/search")
    public List<BuildingEntity> searchByEupMyeonDong(
            @RequestParam(required = false) String eupMyeonDong) {
        return buildingRepo.searchByEupMyeonDong(eupMyeonDong);
    }

    @GetMapping("/search/assigned")
    public List<BuildingEntity> assignedResearcher(
        @RequestParam(required = false) String eupMyeonDong) {
        return buildingRepo.assignedResearcher(eupMyeonDong);
    }
    // 📌 [추가] 주소(lotAddress)로 위도/경도 조회
    @GetMapping("/coords")
    public ResponseEntity<?> getCoordsByAddress(@RequestParam String address) {
        return buildingRepo.findByLotAddress(address)
                .map(b -> Map.of(
                        "latitude", b.getLatitude(),
                        "longitude", b.getLongitude()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 📌 건물 배정 API
    @PostMapping("/assign")
    @Transactional
    public ResponseEntity<?> assignBuildings(@RequestBody AssignRequestDTO req) {
        // 1. 유저 조회
        UserAccountEntity user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("조사자 없음"));

        // 2. 건물 배정
        List<UserBuildingAssignmentEntity> assignments = new ArrayList<>();
        for (Long buildingId : req.getBuildingIds()) {
            UserBuildingAssignmentEntity assign = UserBuildingAssignmentEntity.builder()
                    .buildingId(buildingId)
                    .user(user)
                    .status(1)  // 1 = 배정
                    .build();
            assignments.add(assign);

            // 동시에 건물 status=1로 업데이트
            BuildingEntity building = buildingRepo.findById(buildingId)
                    .orElseThrow(() -> new IllegalArgumentException("건물 없음"));
            building.setStatus(1);
            buildingRepo.save(building);
        }

        assignmentRepo.saveAll(assignments);

        return ResponseEntity.ok(Map.of("success", true, "assignedCount", assignments.size()));
    }

    // 조사 목록 리스트 생성 부분
    @PostMapping
    public ResponseEntity<String> createBuilding(@RequestBody BuildingDTO dto) {
        BuildingEntity entity = new BuildingEntity();

        entity.setLotAddress(dto.getLotAddress());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setBuildingName(dto.getBuildingName());
        entity.setMainUseName(dto.getMainUseName());
        entity.setStructureName(dto.getStructureName());
        entity.setGroundFloors(dto.getGroundFloors());
        entity.setBasementFloors(dto.getBasementFloors());
        entity.setLandArea(dto.getLandArea());
        entity.setBuildingArea(dto.getBuildingArea());

        entity.setStatus(0);

        buildingRepo.save(entity);

        return ResponseEntity.ok("저장 완료");
    }

    @GetMapping("/surveys")
    public PageResponseDto<BuildingListItemDto> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(Math.max(0, page-1), Math.max(1, size), Sort.by(Sort.Direction.DESC, "id"));
        var data = buildingRepo.searchBuildings(keyword, filter, pageable); // ← 메서드 사용
        var items = data.getContent().stream().map(BuildingListItemDto::from).toList();
        return new PageResponseDto<>(items, data.getTotalElements(), data.getTotalPages(), data.getNumber()+1, data.getSize());
    }

    // ✅ 조사원 배정 O + 결재자 미배정 목록 (필터: eupMyeonDong 선택 가능)
//    예) GET /web/building/pending-approval?eupMyeonDong=강동
    @GetMapping("/pending-approval")
    public List<Map<String, Object>> getPendingApproval(@RequestParam(required = false) String eupMyeonDong) {
        var rows = assignmentRepo.findAssignedWithoutApprover(eupMyeonDong); // ↓ 레포 추가 필요
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (var r : rows) {
            Long researcherId = r.getUserId();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("lotAddress", r.getLotAddress());
            m.put("roadAddress", r.getRoadAddress());
            m.put("buildingName", r.getBuildingName());
            m.put("emd", r.getEmd());

            // 프론트 호환 필드 (오른쪽 빨간 박스에 표시)
            m.put("userId", researcherId);
            m.put("user", Map.of("id", researcherId));  // addr.user?.id 케이스 호환

            // 결재자 미배정
            m.put("approvalId", r.getApprovalId());     // 항상 null

            // 프론트의 isAssigned() 방어 통과용
            m.put("status", 1);
            m.put("assigned", true);

            result.add(m);
        }
        return result;
    }

    // ✅ 결재자 배정 (조사원은 이미 배정되어 있어야 함)
//    예) POST /web/building/assign-approver  { "userId": 123, "buildingIds": [1,2,3] }
    @PostMapping("/assign-approver")
    @Transactional
    public ResponseEntity<?> assignApprover(@RequestBody AssignRequestDTO req) {
        var approver = userRepo.findById(req.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("결재자 없음: " + req.getUserId()));

        int count = 0;
        for (Long buildingId : req.getBuildingIds()) {
            var uba = assignmentRepo.findByBuildingId(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("배정 정보가 없습니다. buildingId=" + buildingId));

            // 조사원(Researcher) 미배정이면 정책상 스킵하거나 에러로 처리
            if (uba.getUser() == null && uba.getId() == null) {
                // throw new IllegalStateException("조사원이 미배정입니다. buildingId=" + buildingId);
                continue;
            }

            // 이미 결재자 있으면 스킵(중복 방지)
            if (uba.getApprovalId() != null) continue;

            uba.setApprovalId(approver.getUserId()); // 결재자 배정
            // 상태 진행(선택): 2 = 결재 대기
            if (uba.getStatus() == null || uba.getStatus() < 2) {
                uba.setStatus(2);
            }
            count++;
        }
        return ResponseEntity.ok(Map.of("success", true, "assignedCount", count));
    }



}
