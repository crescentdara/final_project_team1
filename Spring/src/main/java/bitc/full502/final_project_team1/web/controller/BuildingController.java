package bitc.full502.final_project_team1.web.controller;

import bitc.full502.final_project_team1.web.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.web.domain.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/building")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingRepository repository;

    // 📌 전체 건물 목록 조회
    @GetMapping
    public List<BuildingEntity> getAllBuildings() {
        return repository.findAll();
    }

    // 📌 특정 ID로 건물 조회
    @GetMapping("/{id}")
    public BuildingEntity getBuildingById(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }

    // 📌 건물명 리스트 조회 (중복 제거 + 빈 값 제외)
    @GetMapping("/names")
    public List<String> getBuildingNames() {
        return repository.findAll()
                .stream()
                .map(BuildingEntity::getBuildingName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .toList();
    }

    // 📌 lotAddress만 단독으로 조회 (중복 제거 + 빈 값 제외)
    @GetMapping("/addresses")
    public List<String> getBuildingAddresses() {
        return repository.findAll()
                .stream()
                .map(BuildingEntity::getLotAddress)
                .filter(addr -> addr != null && !addr.isBlank())
                .distinct()
                .toList();
    }

    // 📌 lotAddress + 번-지 (+ 보조정보) 조회 (중복 제거)
    @GetMapping("/address-details")
    public List<String> getBuildingAddressDetails() {
        return repository.findAll()
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
        return repository.findDistinctEupMyeonDong(city);
    }

    // 📌 읍면동 기준 검색
    @GetMapping("/search")
    public List<BuildingEntity> searchByEupMyeonDong(@RequestParam(required = false) String eupMyeonDong) {
        return repository.searchByEupMyeonDong(eupMyeonDong);
    }

    // 📌 [추가] 주소(lotAddress)로 위도/경도 조회
    @GetMapping("/coords")
    public ResponseEntity<?> getCoordsByAddress(@RequestParam String address) {
        return repository.findByLotAddress(address)
                .map(b -> Map.of(
                        "latitude", b.getLatitude(),
                        "longitude", b.getLongitude()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
