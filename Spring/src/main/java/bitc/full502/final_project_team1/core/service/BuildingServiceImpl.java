package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.app.dto.AppBuildingDetailDto;
import bitc.full502.final_project_team1.api.web.dto.UploadResultDTO;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import bitc.full502.final_project_team1.core.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


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

    /** 셀 값 문자열 변환 */
    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }


    /** 📌 엑셀 업로드 → DB 저장 */
    @Override
    public UploadResultDTO saveBuildingsFromExcel(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 비어 있습니다.");
        }

        List<BuildingEntity> entities = new ArrayList<>();
        Map<String, List<Integer>> failMap = new HashMap<>();
        int successCount = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                List<String> rowErrors = new ArrayList<>();

                // 셀 값 읽기
                String lotAddress = getCellValue(row.getCell(0));
                String latitude = getCellValue(row.getCell(1));
                String longitude = getCellValue(row.getCell(2));
                String buildingName = getCellValue(row.getCell(3));
                String mainUseName = getCellValue(row.getCell(4));
                String structureName = getCellValue(row.getCell(5));
                String groundFloors = getCellValue(row.getCell(6));
                String basementFloors = getCellValue(row.getCell(7));
                String landArea = getCellValue(row.getCell(8));
                String buildingArea = getCellValue(row.getCell(9));

                // ✅ 필수값 누락 체크
                if (lotAddress == null || lotAddress.isBlank() ||
                        latitude == null || latitude.isBlank() ||
                        longitude == null || longitude.isBlank() ||
                        buildingName == null || buildingName.isBlank() ||
                        mainUseName == null || mainUseName.isBlank() ||
                        structureName == null || structureName.isBlank() ||
                        groundFloors == null || groundFloors.isBlank() ||
                        basementFloors == null || basementFloors.isBlank() ||
                        landArea == null || landArea.isBlank() ||
                        buildingArea == null || buildingArea.isBlank()) {
                    rowErrors.add("필수 값 누락");
                }

                // 숫자 변환 검사
                parseDoubleSafe(latitude, i + 1, "위도", rowErrors);
                parseDoubleSafe(longitude, i + 1, "경도", rowErrors);
                parseIntSafe(groundFloors, i + 1, "지상층수", rowErrors);
                parseIntSafe(basementFloors, i + 1, "지하층수", rowErrors);
                parseDoubleSafe(landArea, i + 1, "대지면적", rowErrors);
                parseDoubleSafe(buildingArea, i + 1, "건축면적", rowErrors);

                if (rowErrors.isEmpty()) {
                    BuildingEntity entity = new BuildingEntity();
                    entity.setLotAddress(lotAddress);
                    entity.setBuildingName(buildingName);
                    entity.setMainUseName(mainUseName);
                    entity.setStructureName(structureName);
                    entity.setLatitude(Double.parseDouble(latitude));
                    entity.setLongitude(Double.parseDouble(longitude));
                    entity.setGroundFloors(Integer.parseInt(groundFloors.split("\\.")[0]));
                    entity.setBasementFloors(Integer.parseInt(basementFloors.split("\\.")[0]));
                    entity.setLandArea(Double.parseDouble(landArea));
                    entity.setBuildingArea(Double.parseDouble(buildingArea));
                    entity.setStatus(0);

                    entities.add(entity);
                    successCount++;
                } else {
                    for (String reason : rowErrors) {
                        failMap.computeIfAbsent(reason, k -> new ArrayList<>()).add(i + 1);
                    }
                }
            }
        }

        if (!entities.isEmpty()) {
            buildingRepository.saveAll(entities);
        }

        // ✅ 최종 failMessages 요약 생성
        List<String> failMessages = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : failMap.entrySet()) {
            List<Integer> rows = entry.getValue();
            int count = rows.size();
            int firstRow = rows.get(0);
            if (count == 1) {
                failMessages.add(entry.getKey() + " (" + firstRow + "행)");
            } else {
                failMessages.add(entry.getKey() + " (" + firstRow + "행 외 " + (count - 1) + "건)");
            }
        }

        return UploadResultDTO.builder()
                .successCount(successCount)
                .failCount(failMessages.size())
                .failMessages(failMessages)
                .build();
    }

    private Double parseDoubleSafe(String value, int rowIndex, String fieldName, List<String> errors) {
        if (value == null || value.isBlank()) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            errors.add(fieldName + " 숫자 변환 실패");
            return null;
        }
    }

    private Integer parseIntSafe(String value, int rowIndex, String fieldName, List<String> errors) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.split("\\.")[0]);
        } catch (NumberFormatException e) {
            errors.add(fieldName + " 숫자 변환 실패");
            return null;
        }
    }


}
