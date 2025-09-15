//package bitc.full502.final_project_team1.web.loader;
//
//@Component
//@RequiredArgsConstructor
//public class CsvDataLoader implements CommandLineRunner {
//
//    private final LandSurveyRepository landSurveyRepository;
//
//    @Override
//    public void run(String... args) throws Exception {
//        // resources/data 밑 파일 불러오기
//        ClassPathResource resource =
//                new ClassPathResource("data/국토교통부_지적재조사 일필지 조사 정보_20250711.csv");
//
//        try (CSVReader reader = new CSVReader(
//                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
//
//            List<String[]> rows = reader.readAll();
//
//            // 첫 줄(헤더) 스킵
//            for (int i = 1; i < rows.size(); i++) {
//                String[] row = rows.get(i);
//
//                LandSurveyEntity land = LandSurveyEntity.builder()
//                        .projectId(row[0])                     // 사업지구번호
//                        .standardYear(Integer.parseInt(row[1])) // 기준년도
//                        .regionCode(row[2])                    // 시군구코드
//                        .regionName(row[3])                    // 시군구명
//                        .projectName(row[4])                   // 사업지구명
//                        .landUniqueCode(row[5])                // 토지고유코드
//                        .landTypeCode(row[6])                  // 토지임야대장 지목코드
//                        .landArea(Double.parseDouble(row[7]))  // 토지임야대장 면적
//                        // row[8] = 조사자 의견내용 (제외)
//                        .buildingSerial(row[9])                // 건축물 일련번호
//                        .floorAbove(parseIntSafe(row[10]))     // 지상층수
//                        .floorBelow(parseIntSafe(row[11]))     // 지하층수
//                        .buildingArea(parseDoubleSafe(row[12]))// 건축면적
//                        .buildingCoverage(parseDoubleSafe(row[13])) // 건폐율
//                        .floorAreaRatio(parseDoubleSafe(row[14]))   // 용적률
//                        .structureCode(row[15])                // 건축물구조코드
//                        .structureName(row[16])                // 건축물구조코드명
//                        .usageCode(row[17])                    // 건축물용도코드
//                        .usageName(row[18])                    // 건축물용도코드명
//                        .build();
//
//                landSurveyRepository.save(land);
//            }
//        }
//    }
//
//    // 안전한 Integer 파싱 (빈칸일 경우 null)
//    private Integer parseIntSafe(String value) {
//        try {
//            return (value == null || value.isBlank()) ? null : Integer.parseInt(value);
//        } catch (NumberFormatException e) {
//            return null;
//        }
//    }
//
//    // 안전한 Double 파싱 (빈칸일 경우 null)
//    private Double parseDoubleSafe(String value) {
//        try {
//            return (value == null || value.isBlank()) ? null : Double.parseDouble(value);
//        } catch (NumberFormatException e) {
//            return null;
//        }
//    }
//}
