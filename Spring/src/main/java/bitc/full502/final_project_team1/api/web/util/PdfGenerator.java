package bitc.full502.final_project_team1.api.web.util;

import bitc.full502.final_project_team1.api.web.dto.ResultDetailDto;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Paths; // 🔧
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class PdfGenerator {

    @Value("${file.report-dir}")
    private String reportDir;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String generateSurveyReport(ResultDetailDto detail,
                                       UserAccountEntity approver,
                                       String clientId,
                                       String clientSecret) {
        try {
            // === PDF 저장 경로 ===
            File dir = new File(reportDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = "report-" + detail.getId() + ".pdf";
            var filePath = Paths.get(reportDir, fileName);

            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
            document.open();

            // ✅ 한글 폰트
            BaseFont bfKorean = BaseFont.createFont("c:/windows/fonts/malgun.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font coverTitleFont = new Font(bfKorean, 24, Font.BOLD, Color.BLACK);
            Font coverSubFont   = new Font(bfKorean, 14, Font.NORMAL, Color.DARK_GRAY);
            Font infoFont       = new Font(bfKorean, 12, Font.NORMAL, Color.BLACK);
            Font sectionFont    = new Font(bfKorean, 14, Font.BOLD, Color.BLACK);

            PdfContentByte cb = writer.getDirectContent();

            // ------------------ 1. 표지 ------------------
            ColumnText.showTextAligned(
                    cb, Element.ALIGN_CENTER,
                    new Phrase("건축물 현장조사 보고서", coverTitleFont),
                    document.getPageSize().getWidth() / 2,
                    document.getPageSize().getHeight() / 2 + 60,
                    0
            );

            ColumnText.showTextAligned(
                    cb, Element.ALIGN_CENTER,
                    new Phrase("사례 번호: M-" + detail.getId(), coverSubFont),
                    document.getPageSize().getWidth() / 2,
                    document.getPageSize().getHeight() / 2 + 30,
                    0
            );

            Paragraph footerPara = new Paragraph();
            footerPara.setAlignment(Element.ALIGN_CENTER);
            footerPara.add(new Phrase("조사자: " + detail.getInvestigator(), infoFont));
            footerPara.add(Chunk.NEWLINE);
            footerPara.add(new Phrase("결재자: " + approver.getName() +
                    " (" + approver.getUsername() + ")", infoFont));
            footerPara.add(Chunk.NEWLINE);
            footerPara.add(new Phrase("승인일시: " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now()), infoFont));

            ColumnText ct = new ColumnText(cb);
            ct.setSimpleColumn(
                    document.getPageSize().getWidth() / 2 - 200,
                    document.getPageSize().getHeight() / 2 - 80,
                    document.getPageSize().getWidth() / 2 + 200,
                    document.getPageSize().getHeight() / 2 - 10
            );
            ct.addElement(footerPara);
            ct.go();

            document.newPage();

            // ------------------ 2. 본문 ------------------
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 7.5f});

            addGrayRow(table, "사례 번호", "M-" + detail.getId(), bfKorean);
            addGrayRow(table, "조사자", detail.getInvestigator(), bfKorean);
            addGrayRow(table, "주소", detail.getAddress(), bfKorean);
            addGrayRow(table, "결재자", approver.getName() + "(" + approver.getUsername() + ")", bfKorean);
            addGrayRow(table, "승인일시",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), bfKorean);

            addGrayRow(table, "조사 가능 여부", mapPossible(detail.getPossible()), bfKorean);
            addGrayRow(table, "행정 목적 활용", mapAdminUse(detail.getAdminUse()), bfKorean);
            addGrayRow(table, "유휴 비율", mapIdleRate(detail.getIdleRate()), bfKorean);
            addGrayRow(table, "안전 등급", mapSafety(detail.getSafety()), bfKorean);
            addGrayRow(table, "외벽 상태", mapState(detail.getWall()), bfKorean);
            addGrayRow(table, "옥상 상태", mapState(detail.getRoof()), bfKorean);
            addGrayRow(table, "창호 상태", mapState(detail.getWindowState()), bfKorean);
            addGrayRow(table, "주차 가능", mapParking(detail.getParking()), bfKorean);
            addGrayRow(table, "현관 상태", mapState(detail.getEntrance()), bfKorean);
            addGrayRow(table, "천장 상태", mapState(detail.getCeiling()), bfKorean);
            addGrayRow(table, "바닥 상태", mapState(detail.getFloor()), bfKorean);
            addGrayRow(table, "외부 기타 사항", detail.getExtEtc(), bfKorean);
            addGrayRow(table, "내부 기타 사항", detail.getIntEtc(), bfKorean);

            document.add(table);

            // ------------------ 4. 사진 (4칸 그리드) ------------------
            document.newPage();

            addPhotoQuad(document, bfKorean, detail); // 🔧 기존 addImageIfExists 4회 호출 대신 4열 테이블로 렌더

            // ------------------ 3. 지도 이미지 ------------------
            if (detail.getLatitude() != null && detail.getLongitude() != null) {
                try {
                    String mapUrl = String.format(
                            "https://static-maps.yandex.ru/1.x/?ll=%f,%f&z=16&size=600,400&l=map&pt=%f,%f,pm2rdm",
                            detail.getLongitude(), detail.getLatitude(),
                            detail.getLongitude(), detail.getLatitude()
                    );

                    Image mapImg = Image.getInstance(new URL(mapUrl));
                    mapImg.scaleToFit(500, 350);
                    mapImg.setSpacingBefore(15);  // 사진과 간격
                    mapImg.setSpacingAfter(15);

                    document.add(new Paragraph("건물 위치 지도", sectionFont));
                    document.add(mapImg);

                } catch (Exception ex) {
                    document.add(new Paragraph("지도 이미지 불러오기 실패: " + ex.getMessage()));
                }
            }

            document.close();
//            return filePath.toString();
            return fileName;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("PDF 생성 실패", e);
        }
    }

    // ================= 헬퍼 메서드 =================

    private static void addGrayRow(PdfPTable table, String label, String value, BaseFont bf) {
        Font keyFont = new Font(bf, 11, Font.BOLD, Color.BLACK);
        Font valFont = new Font(bf, 11, Font.NORMAL, Color.DARK_GRAY);

        PdfPCell keyCell = new PdfPCell(new Phrase(label, keyFont));
        keyCell.setBackgroundColor(new Color(240, 240, 240));
        keyCell.setPadding(10);
        keyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        keyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell valCell = new PdfPCell(new Phrase(
                (value == null || value.isBlank()) ? "-" : value, valFont));
        valCell.setBackgroundColor(Color.WHITE);
        valCell.setPadding(10);

        table.addCell(keyCell);
        table.addCell(valCell);
    }

    // 🔧 추가: 상대 경로("/upload/...")를 실제 파일 경로로 변환
    private File resolveFromRelative(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return null;
        String p = relativePath.replace('\\', '/');
        if (p.startsWith("/")) p = p.substring(1);
        if (p.startsWith("upload/")) p = p.substring("upload/".length());
        return Paths.get(uploadDir).resolve(p).normalize().toFile(); // uploadDir 하위
    }

    // 🔧 추가: 사진 4칸(라벨 행 + 회색박스 이미지 행)
    private void addPhotoQuad(Document document, BaseFont bf, ResultDetailDto d) throws Exception {
        Font labelFont = new Font(bf, 11, Font.BOLD, Color.DARK_GRAY);
        Font emptyFont = new Font(bf, 10, Font.NORMAL, new Color(120, 120, 120));

        // 요청 명칭 고정: 외부 사진, 외부 사진 수정, 내부 사진, 내부 사진 수정
        String[][] items = new String[][]{
                {"외부 사진",      d.getExtPhoto()},
                {"외부 사진 수정", d.getExtEditPhoto()},
                {"내부 사진",      d.getIntPhoto()},
                {"내부 사진 수정", d.getIntEditPhoto()}
        };

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1, 1, 1});
        table.setSpacingBefore(12f);
        table.setSpacingAfter(10f);

        // 1행: 라벨
        for (String[] it : items) {
            PdfPCell c = new PdfPCell(new Phrase(it[0], labelFont));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            c.setBorder(Rectangle.NO_BORDER); // 라벨은 외곽선 없음
            c.setPaddingBottom(4f);
            table.addCell(c);
        }

        // 2행: 회색 박스 + 이미지
        for (String[] it : items) {
            File file = resolveFromRelative(it[1]);

            PdfPCell box = new PdfPCell();
            box.setFixedHeight(170f);                      // 박스 높이
            box.setPadding(6f);
            box.setBorderWidth(1.2f);
            box.setBorderColor(new Color(200, 200, 200));  // 회색 테두리
            box.setBackgroundColor(Color.WHITE);
            box.setHorizontalAlignment(Element.ALIGN_CENTER);
            box.setVerticalAlignment(Element.ALIGN_MIDDLE);

            if (file != null && file.exists()) {
                Image img = Image.getInstance(file.getAbsolutePath());
                img.setAlignment(Image.ALIGN_CENTER);
                img.scaleToFit(130f, 150f);                // 박스 내에 맞춤
                box.addElement(img);
            } else {
                Paragraph empty = new Paragraph("이미지 없음", emptyFont);
                empty.setAlignment(Element.ALIGN_CENTER);
                box.addElement(empty);
            }
            table.addCell(box);
        }

        document.add(table);
    }

    // ================= 매핑 메서드 =================
    private static String mapPossible(Integer v) {
        return v == null ? "-" : (v == 1 ? "가능" : v == 2 ? "불가" : "-");
    }

    private static String mapAdminUse(Integer v) {
        return v == null ? "-" : switch (v) {
            case 1 -> "활용";
            case 2 -> "일부활용";
            case 3 -> "미활용";
            default -> "-";
        };
    }

    private static String mapIdleRate(Integer v) {
        return v == null ? "-" : switch (v) {
            case 1 -> "0~10%";
            case 2 -> "10~30%";
            case 3 -> "30~50%";
            case 4 -> "50%+";
            default -> "-";
        };
    }

    private static String mapSafety(Integer v) {
        return v == null ? "-" : switch (v) {
            case 1 -> "A";
            case 2 -> "B";
            case 3 -> "C";
            case 4 -> "D";
            case 5 -> "E";
            default -> "-";
        };
    }

    private static String mapState(Integer v) {
        return v == null ? "-" : switch (v) {
            case 1 -> "양호";
            case 2 -> "보통";
            case 3 -> "불량";
            default -> "-";
        };
    }

    private static String mapParking(Integer v) {
        return v == null ? "-" : (v == 1 ? "가능" : v == 2 ? "불가" : "-");
    }
}
