package bitc.full502.final_project_team1.web.controller;

import bitc.full502.final_project_team1.web.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.web.domain.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                .filter(name -> name != null && !name.isBlank()) // 빈 값 제외
                .distinct() // ✅ 중복 제거
                .toList();
    }

    // 📌 lotAddress만 단독으로 조회 (중복 제거 + 빈 값 제외)
    @GetMapping("/addresses")
    public List<String> getBuildingAddresses() {
        return repository.findAll()
                .stream()
                .map(BuildingEntity::getLotAddress)
                .filter(addr -> addr != null && !addr.isBlank()) // 빈 값 제외
                .distinct() // ✅ 중복 제거
                .toList();
    }

    // 📌 lotAddress + 번-지 (+ 보조정보) 조회 (중복 제거)
    @GetMapping("/address-details")
    public List<String> getBuildingAddressDetails() {
        return repository.findAll()
                .stream()
                .map(b -> {
                    StringBuilder sb = new StringBuilder();

                    // 기본 주소
                    if (b.getLotAddress() != null && !b.getLotAddress().isBlank()) {
                        sb.append(b.getLotAddress());
                    }

                    boolean hasMain = b.getLotMainNo() != null && !b.getLotMainNo().isBlank() && !"0".equals(b.getLotMainNo());
                    boolean hasSub = b.getLotSubNo() != null && !b.getLotSubNo().isBlank() && !"0".equals(b.getLotSubNo());

                    // 번-지 붙이기 (0은 무시)
                    if (hasMain) {
                        sb.append(" ").append(b.getLotMainNo());
                        if (hasSub) {
                            sb.append("-").append(b.getLotSubNo());
                        }
                        sb.append("번지");
                    }
                    else {
                        // 번지가 없으면 → 도로명주소 우선 표시
                        if (b.getRoadAddress() != null && !b.getRoadAddress().isBlank()) {
                            sb.append(" (").append(b.getRoadAddress()).append(")");
                        }
                        // 도로명주소도 없으면 → 건물명 표시
                        else if (b.getBuildingName() != null && !b.getBuildingName().isBlank()) {
                            sb.append(" - ").append(b.getBuildingName());
                        }
                    }

                    return sb.toString();
                })
                .filter(addr -> addr != null && !addr.isBlank()) // 빈 값 제외
                .distinct() // ✅ 중복 제거
                .toList();
    }
}
