package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.*;

import bitc.full502.final_project_team1.core.domain.enums.Role;

import bitc.full502.final_project_team1.core.domain.entity.ApprovalEntity;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
import bitc.full502.final_project_team1.core.domain.repository.ApprovalRepository;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserBuildingAssignmentRepository;
import bitc.full502.final_project_team1.core.service.BuildingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/web/building")
@RequiredArgsConstructor
public class BuildingController {

  private final BuildingRepository buildingRepo;
  private final UserAccountRepository userRepo;
  private final UserBuildingAssignmentRepository assignmentRepo;
  private final BuildingService buildingService;
  private final ApprovalRepository approvalRepo;

  // 전체 건물 목록 조회
  @GetMapping
  public List<BuildingEntity> getAllBuildings() {
    return buildingRepo.findAll();
  }

  // 특정 ID로 건물 조회
  @GetMapping("/{id}")
  public BuildingEntity getBuildingById(@PathVariable Long id) {
    return buildingRepo.findById(id).orElse(null);
  }

  // 건물명 리스트 조회 (중복 제거 + 빈 값 제외)
  @GetMapping("/names")
  public List<String> getBuildingNames() {
    return buildingRepo.findAll()
        .stream()
        .map(BuildingEntity::getBuildingName)
        .filter(name -> name != null && !name.isBlank())
        .distinct()
        .toList();
  }

  // lotAddress만 단독으로 조회 (중복 제거 + 빈 값 제외)
  @GetMapping("/addresses")
  public List<String> getBuildingAddresses() {
    return buildingRepo.findAll()
        .stream()
        .map(BuildingEntity::getLotAddress)
        .filter(addr -> addr != null && !addr.isBlank())
        .distinct()
        .toList();
  }

  // lotAddress + 번-지 (+ 보조정보) 조회 (중복 제거)
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

  @GetMapping("/surveys")
  public PageResponseDto<BuildingListItemDto> list(
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(defaultValue = "ALL") String filter,
      @RequestParam(defaultValue = "1")  int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    var pageable = PageRequest.of(Math.max(0, page-1), Math.max(1, size),
        Sort.by(Sort.Direction.DESC, "id"));

    // 🔹 Projection 기반 조회 (검색/필터 포함)
    var data = buildingRepo.searchBuildings(keyword, filter, pageable);

    // 🔹 DTO 변환 (statusLabel 포함)
    var items = data.getContent().stream()
        .map(BuildingListItemDto::from)
        .toList();

    return new PageResponseDto<>(
        items,
        data.getTotalElements(),
        data.getTotalPages(),
        data.getNumber() + 1,
        data.getSize()
    );
  }

  // 📌 [수정] 미배정 조사지 + 조사원 목록 조회 (전체 리스트 반환)
  @GetMapping("/unassigned")
  public Map<String, Object> getUnassignedBuildings(
      @RequestParam(required = false) String region,
      @RequestParam(required = false) String keyword
  ) {
    // 1. 미배정 건물(status=0)
    List<BuildingEntity> results = buildingRepo.findUnassignedByRegion(region);

    // 2. 조사원 조회 (region + keyword 반영)
    List<UserAccountEntity> investigators;
    if (region == null || region.isBlank()) {
      investigators = (keyword == null || keyword.isBlank())
          ? userRepo.findByRole(Role.RESEARCHER)
          : userRepo.findByRoleAndKeyword(Role.RESEARCHER, keyword);
    } else {
      investigators = (keyword == null || keyword.isBlank())
          ? userRepo.findByRoleAndPreferredRegionLike(Role.RESEARCHER, region)
          : userRepo.findByRoleAndPreferredRegionAndKeyword(Role.RESEARCHER, region, keyword);
    }

    // 3. 응답
    return Map.of(
        "results", results,
        "totalResults", results.size(),
        "investigators", investigators
    );
  }

  // 조사 목록 리스트 생성 부분 (단건 등록)
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

  // 엑셀을 이용한 조사 목록 리스트 생성 (다건 등록)
  @PostMapping("/upload-excel")
  public ResponseEntity<UploadResultDTO> uploadBuildings(@RequestParam("file") MultipartFile file) {
    try {
      UploadResultDTO uploadResult = buildingService.saveBuildingsFromExcel(file);
      return ResponseEntity.ok(uploadResult);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(
          UploadResultDTO.builder()
              .successCount(0)
              .failCount(1)
              .failMessages(List.of("업로드 실패: " + e.getMessage()))
              .build()
      );
    }
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

  // 조사지 목록 삭제
  @DeleteMapping("/{id}")
  @Transactional
  public ResponseEntity<?> deleteBuildingHard(@PathVariable Long id) {
    // 존재 확인(없으면 204로 바로 반환해도 OK)
    if (!buildingRepo.existsById(id)) {
      return ResponseEntity.noContent().build();
    }
    try {
      // 1) 배정(UBA) 먼저 삭제 (approval_id FK 때문에 approval보다 먼저)
      int uba = assignmentRepo.deleteByBuildingId(id);

      // 2) 결재(approval) 삭제
      int apr = approvalRepo.deleteByBuildingId(id);

      // 3) (선택) 조사결과(survey_result)도 building_id FK가 있다면 여기서 삭제
      // surveyResultRepo.deleteByBuildingId(id);

      // 4) 마지막으로 building 삭제
      buildingRepo.deleteById(id);

      return ResponseEntity.noContent().build(); // 204
      // 또는: return ResponseEntity.ok(Map.of("deletedId", id, "deletedAssignments", uba, "deletedApprovals", apr));
    } catch (DataIntegrityViolationException ex) {
      // 다른 FK로 막히는 경우 명확한 메시지
      return ResponseEntity.status(409)
          .body("다른 데이터가 참조 중이라 삭제할 수 없습니다. 관련 배정/결재/조사결과를 먼저 정리하세요.");
    }
  }

  /**
   * ✅ 조사지(건물) 수정
   * React: PUT /web/building/{id}
   * Body: CreateSurvey가 보내는 payload (BuildingDTO 필드명과 1:1)
   * - 문자열은 null이 오면 무시(기존 유지), 값이 오면 그대로 반영(비워서 "" 보내면 빈문자 저장)
   * - 숫자는 null이면 무시, 값이 오면 반영
   * - 배정/결재와 무관 (이 API는 순수 메타데이터 수정만)
   */
  @PutMapping("/{id}")
  @Transactional
  public ResponseEntity<?> updateBuilding(
      @PathVariable Long id,
      @RequestBody BuildingDTO dto
  ) {
    BuildingEntity b = buildingRepo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("건물을 찾을 수 없습니다. id=" + id));

    // ---- 문자열 계열 (null이 아닌 값만 반영) ----
    if (dto.getLotAddress()    != null) b.setLotAddress(dto.getLotAddress());
    if (dto.getBuildingName()  != null) b.setBuildingName(dto.getBuildingName());
    if (dto.getMainUseName()   != null) b.setMainUseName(dto.getMainUseName());     // ← React mainUseName
    if (dto.getStructureName() != null) b.setStructureName(dto.getStructureName()); // ← React structureName

    // ---- 숫자 계열 (null이 아닌 값만 반영) ----
    if (dto.getLatitude()       != null) b.setLatitude(dto.getLatitude());
    if (dto.getLongitude()      != null) b.setLongitude(dto.getLongitude());
    if (dto.getGroundFloors()   != null) b.setGroundFloors(dto.getGroundFloors());
    if (dto.getBasementFloors() != null) b.setBasementFloors(dto.getBasementFloors());
    if (dto.getLandArea()       != null) b.setLandArea(dto.getLandArea());
    if (dto.getBuildingArea()   != null) b.setBuildingArea(dto.getBuildingArea());

    buildingRepo.save(b);

    return ResponseEntity.ok(Map.of(
        "updated", true,
        "id", b.getId()
    ));
  }
}
