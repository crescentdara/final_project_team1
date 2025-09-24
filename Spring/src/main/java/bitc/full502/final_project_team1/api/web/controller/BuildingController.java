package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.AssignRequestDTO;
import bitc.full502.final_project_team1.api.web.dto.BuildingDTO;
import bitc.full502.final_project_team1.api.web.dto.BuildingListItemDto;
import bitc.full502.final_project_team1.api.web.dto.PageResponseDto;
import bitc.full502.final_project_team1.core.domain.entity.ApprovalEntity;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
import bitc.full502.final_project_team1.core.domain.repository.ApprovalRepository;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserBuildingAssignmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/web/building")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingRepository buildingRepo;
    private final UserAccountRepository userRepo;
    private final UserBuildingAssignmentRepository assignmentRepo;
    private final ApprovalRepository approvalRepo; // ✅ 추가

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
                } else {
                    if (b.getRoadAddress() != null && !b.getRoadAddress().isBlank()) {
                        sb.append(" (").append(b.getRoadAddress()).append(")");
                    } else if (b.getBuildingName() != null && !b.getBuildingName().isBlank()) {
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

    // 📌 조사원 배정 API — ✅ UPSERT로 중복키 방지
    @PostMapping("/assign")
    @Transactional
    public ResponseEntity<?> assignBuildings(@RequestBody AssignRequestDTO req) {
        UserAccountEntity user = userRepo.findById(req.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("조사자 없음"));

        int created = 0, updated = 0, skipped = 0;

        for (Long buildingId : req.getBuildingIds()) {
            BuildingEntity building = buildingRepo.findById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("건물 없음: " + buildingId));

            var existing = assignmentRepo.findByBuildingId(buildingId);

            if (existing.isPresent()) {
                // 이미 배정 행이 있으면 UPDATE
                var uba = existing.get();

                if (uba.getUser() != null && Objects.equals(uba.getUser().getUserId(), user.getUserId())) {
                    skipped++; // 같은 사람에게 이미 배정
                } else {
                    uba.setUser(user);
                    uba.setStatus(1);
                    uba.setAssignedAt(LocalDateTime.now());
                    uba.setApprovalId(null); // 재배정 시 결재 초기화(정책)
                    building.setAssignedUserId(user.getUserId());
                    assignmentRepo.save(uba);
                    updated++;
                }
            } else {
                // 없으면 INSERT
                var uba = UserBuildingAssignmentEntity.builder()
                    .buildingId(buildingId)
                    .user(user)
                    .status(1)
                    .assignedAt(LocalDateTime.now())
                    .approvalId(null)
                    .build();
                assignmentRepo.save(uba);
                created++;
            }

            // 건물 상태 동기화
            building.setStatus(1);
            building.setAssignedUser(user);
            buildingRepo.save(building);
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "created", created,
            "updated", updated,
            "skipped", skipped,
            "assignedCount", created + updated
        ));
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
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size), Sort.by(Sort.Direction.DESC, "id"));
        var data = buildingRepo.searchBuildings(keyword, filter, pageable);
        var items = data.getContent().stream().map(BuildingListItemDto::from).toList();
        return new PageResponseDto<>(items, data.getTotalElements(), data.getTotalPages(), data.getNumber() + 1, data.getSize());
    }

    @GetMapping("/pending-approval")
    public List<Map<String, Object>> pendingApproval(
        @RequestParam(required = false) String eupMyeonDong
    ) {
        var rows = assignmentRepo.findAssignedWithoutApprover(eupMyeonDong);
        List<Map<String, Object>> out = new ArrayList<>(rows.size());

        for (var r : rows) {
            Long   researcherId = r.getAssignedUserId();      // 프로젝션 이름과 일치
            String researcherNm = r.getAssignedName();
            String researcherUn = r.getAssignedUsername();

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",           r.getId());
            m.put("lotAddress",   r.getLotAddress());
            m.put("roadAddress",  r.getRoadAddress());
            m.put("buildingName", r.getBuildingName());

            // 좌측 파란박스에 쓰일 값
            m.put("assignedUserId",   researcherId);
            m.put("assignedName",     researcherNm);
            m.put("assignedUsername", researcherUn);

            // 프론트 호환 필드
            m.put("userId", researcherId);

            // ❗️ Map.of 는 null 불가 → HashMap으로 안전하게
            Map<String, Object> userObj = new HashMap<>();
            if (researcherId != null) {
                userObj.put("id", researcherId);
            }
            m.put("user", userObj);

            m.put("approvalId", r.getApprovalId()); // null 허용
            m.put("status",   1);
            m.put("assigned", true);

            out.add(m);
        }
        return out;
    }

    // ✅ 결재자 배정 (조사원은 이미 배정되어 있어야 함)
    // 예) POST /web/building/assign-approver  { "userId": 123, "buildingIds": [1,2,3] }
    @PostMapping("/assign-approver")
    @Transactional
    public ResponseEntity<?> assignApprover(@RequestBody AssignRequestDTO req) {
        var approver = userRepo.findById(req.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("결재자 없음: " + req.getUserId()));

        int count = 0, skipped = 0;

        for (Long buildingId : req.getBuildingIds()) {
            var uba = assignmentRepo.findByBuildingId(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("배정 정보가 없습니다. buildingId=" + buildingId));

            // 조사원 미배정이면 스킵(정책에 따라 에러로 바꿀 수 있음)
            if (uba.getUser() == null) {
                skipped++;
                continue;
            }

            // 이미 결재자(approval) 연결되어 있으면 스킵
            if (uba.getApprovalId() != null) {
                skipped++;
                continue;
            }

            // Approval 레코드 생성 → approval.id 를 UBA.approval_id 로 연결 (FK 일치)
            var building = buildingRepo.findById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("건물 없음: " + buildingId));

            var approval = approvalRepo.saveAndFlush(
                ApprovalEntity.builder()
                    .building(building)
                    .approver(approver)
                    .surveyor(uba.getUser()) // 조사원
                    .approvedAt(null)        // 대기
                    .rejectReason(null)
                    // surveyResult 는 대기 상태에서 null 가능해야 함
                    .build()
            );

            uba.setApprovalId(approval.getId());
            uba.setStatus(2); // 결재 대기
            assignmentRepo.save(uba);

            count++;
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "assignedCount", count,
            "skipped", skipped
        ));
    }
}
