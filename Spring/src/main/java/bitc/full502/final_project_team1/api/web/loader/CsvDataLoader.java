package bitc.full502.final_project_team1.api.web.loader;

import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class CsvDataLoader implements CommandLineRunner {

    private final BuildingRepository buildingRepository;

    @Override
    public void run(String... args) throws Exception {
        if (buildingRepository.count() > 0) {
            System.out.println("✅ Building 데이터가 이미 존재합니다. CSV 로드를 건너뜁니다.");
            return;
        }

        ClassPathResource resource =
                new ClassPathResource("data/경상남도 김해시_건축물 현황_20240731.csv");

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            List<String[]> rows = reader.readAll();

            // 동별 카운트 관리
            Map<String, Integer> dongCounter = new HashMap<>();
            // 동별 lotAddress 중복 방지
            Map<String, Set<String>> dongLotAddressSet = new HashMap<>();

            int inserted = 0; // 전체 insert 건수 카운트

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);

                String lotAddress = row[1]; // 번지주소
                if (lotAddress == null || lotAddress.isBlank()) {
                    continue; // 주소 없는 데이터는 건너뜀
                }

                // ⚠️ 번, 지 값이 모두 0이거나 비어 있으면 skip
                boolean noMain = (row[2] == null || row[2].isBlank() || "0".equals(row[2].trim()));
                boolean noSub  = (row[3] == null || row[3].isBlank() || "0".equals(row[3].trim()));
                if (noMain && noSub) {
                    continue;
                }

                // "경상남도 김해시 강동 ..." → 세 번째 토큰을 동 이름으로 사용
                String[] parts = lotAddress.split(" ");
                String dongName = parts.length >= 3 ? parts[2] : lotAddress;

                // 동별 set 초기화
                dongLotAddressSet.putIfAbsent(dongName, new HashSet<>());

                // ✅ 이미 같은 lotAddress가 이 동에 들어가 있으면 skip
                if (dongLotAddressSet.get(dongName).contains(lotAddress)) {
                    continue;
                }

                int count = dongCounter.getOrDefault(dongName, 0);
                if (count >= 10) {
                    continue; // 이미 10개 넣었으면 skip
                }

                BuildingEntity building = BuildingEntity.builder()
                        .lotAddress(row[1])
                        .lotMainNo(row[2])
                        .lotSubNo(row[3])
                        .roadAddress(row[4])
                        .ledgerDivisionName(row[5])
                        .ledgerTypeName(row[6])
                        .buildingName(row[7])
                        .extraLotCount(parseIntSafe(row[8]))
                        .newRoadCode(row[9])
                        .newLegalDongCode(row[10])
                        .newMainNo(row[11])
                        .newSubNo(row[12])
                        .mainSubCode(row[13])
                        .mainSubName(row[14])
                        .landArea(parseDoubleSafe(row[15]))
                        .buildingArea(parseDoubleSafe(row[16]))
                        .buildingCoverage(parseDoubleSafe(row[17]))
                        .totalFloorArea(parseDoubleSafe(row[18]))
                        .floorAreaForRatio(parseDoubleSafe(row[19]))
                        .floorAreaRatio(parseDoubleSafe(row[20]))
                        .structureCode(row[21])
                        .structureName(row[22])
                        .etcStructure(row[23])
                        .mainUseCode(row[24])
                        .mainUseName(row[25])
                        .etcUse(row[26])
                        .roofCode(row[27])
                        .roofName(row[28])
                        .etcRoof(row[29])
                        .height(parseDoubleSafe(row[30]))
                        .groundFloors(parseIntSafe(row[31]))
                        .basementFloors(parseIntSafe(row[32]))
                        .passengerElevators(parseIntSafe(row[33]))
                        .emergencyElevators(parseIntSafe(row[34]))
                        .annexCount(parseIntSafe(row[35]))
                        .annexArea(parseDoubleSafe(row[36]))
                        .totalBuildingArea(parseDoubleSafe(row[37]))
                        .latitude(parseDoubleSafe(row[38]))
                        .longitude(parseDoubleSafe(row[39]))
                        .build();

                buildingRepository.save(building);

                // ✅ 저장된 lotAddress 기록
                dongLotAddressSet.get(dongName).add(lotAddress);

                dongCounter.put(dongName, count + 1);
                inserted++;
            }

            System.out.println("✅ CSV 데이터 적재 완료 (총 " + inserted + "건 저장됨)");
        }
    }

    // 안전한 Integer 파싱
    private Integer parseIntSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 안전한 Double 파싱
    private Double parseDoubleSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
