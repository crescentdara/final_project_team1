package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.AppBuildingDetailDto;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;

    @Override
    public AppBuildingDetailDto getBuildingDetail(Long id) {
        BuildingEntity entity = buildingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("건물을 찾을 수 없습니다. id=" + id));

        return AppBuildingDetailDto.builder()
                .id(entity.getId())
                .lotAddress(entity.getLotAddress())
                .roadAddress(entity.getRoadAddress())
                .buildingName(entity.getBuildingName())
                .groundFloors(entity.getGroundFloors())
                .basementFloors(entity.getBasementFloors())
                .totalFloorArea(entity.getTotalFloorArea())
                .landArea(entity.getLandArea())
                .mainUseCode(entity.getMainUseCode())
                .mainUseName(entity.getMainUseName())
                .etcUse(entity.getEtcUse())
                .structureName(entity.getStructureName())
                .height(entity.getHeight())
                .build();
    }
}
