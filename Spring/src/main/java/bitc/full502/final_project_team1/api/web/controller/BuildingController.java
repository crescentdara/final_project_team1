package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.*;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
import bitc.full502.final_project_team1.core.domain.enums.Role;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserBuildingAssignmentRepository;
import bitc.full502.final_project_team1.core.service.BuildingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/web/building")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingRepository buildingRepo;
    private final UserAccountRepository userRepo;
    private final UserBuildingAssignmentRepository assignmentRepo;
    private final BuildingService buildingService;

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
    public List<String> getEupMyeonDong(@RequestParam(defaultValue = "김해시") String city) {
        return buildingRepo.findDistinctEupMyeonDong(city);
    }

    // 📌 읍면동 기준 검색 (미배정만 내려옴)
    @GetMapping("/search")
    public List<BuildingEntity> searchByEupMyeonDong(
            @RequestParam(required = false) String eupMyeonDong) {
        return buildingRepo.searchByEupMyeonDong(eupMyeonDong);
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

}
