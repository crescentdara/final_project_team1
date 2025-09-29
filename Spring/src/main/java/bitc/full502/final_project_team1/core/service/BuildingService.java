package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.AppBuildingDetailDto;
import bitc.full502.final_project_team1.api.web.dto.UploadResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface BuildingService {
    AppBuildingDetailDto getBuildingDetail(Long id);

    // 엑셀 다건 등록
    UploadResultDTO saveBuildingsFromExcel(MultipartFile file) throws Exception;
}
