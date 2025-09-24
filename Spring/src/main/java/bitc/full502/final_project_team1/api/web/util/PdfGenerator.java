package bitc.full502.final_project_team1.api.web.util;

import bitc.full502.final_project_team1.api.web.dto.ResultDetailDto;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PdfGenerator {

    public static String generateSurveyReport(ResultDetailDto detail,
                                              UserAccountEntity approver,
                                              String clientId,
                                              String clientSecret) {
        try {
            String basePath = "reports/";
            File dir = new File(basePath);
            if (!dir.exists()) dir.mkdirs();

            String fileName = basePath + "report-" + detail.getId() + ".pdf";

            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // ✅ 한글 폰트
            BaseFont bfKorean = BaseFont.createFont("c:/windows/fonts/malgun.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font coverTitleFont = new Font(bfKorean, 24, Font.BOLD, Color.BLACK);
            Font coverSubFont = new Font(bfKorean, 14, Font.NORMAL, Color.DARK_GRAY);
            Font infoFont = new Font(bfKorean, 12, Font.NORMAL, Color.BLACK);
            Font sectionFont = new Font(bfKorean, 14, Font.BOLD, Color.BLACK);

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
            footerPara.add(new Phrase("승인자: " + approver.getName() +
                    " (" + approver.getUsername() + ")", infoFont));
            footerPara.add(Chunk.NEWLINE);
            footerPara.add(new Phrase("승인일시: " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.now()), infoFont));

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
            addGrayRow(table, "승인자", approver.getName() + "(" + approver.getUsername() + ")", bfKorean);
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

            // ------------------ 3. 지도 이미지 ------------------
            if (detail.getLatitude() != null && detail.getLongitude() != null) {
                try {
                    String mapUrl = String.format(
                            "https://naveropenapi.apigw.ntruss.com/map-static/v2/raster?w=600&h=400&center=%f,%f&level=16",
                            detail.getLongitude(), detail.getLatitude()
                    );

                    HttpURLConnection conn = (HttpURLConnection) new URL(mapUrl).openConnection();
                    conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
                    conn.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);

                    try (java.io.InputStream is = conn.getInputStream()) {
                        byte[] imageBytes = is.readAllBytes();
                        Image mapImg = Image.getInstance(imageBytes);
                        mapImg.scaleToFit(500, 350);
                        mapImg.setSpacingAfter(15);
                        document.add(new Paragraph("📍 건물 위치 지도", sectionFont));
                        document.add(mapImg);
                    }
                } catch (Exception ex) {
                    document.add(new Paragraph("지도 이미지 불러오기 실패: " + ex.getMessage()));
                }
            }

            // ------------------ 4. 사진 ------------------
            addImageIfExists(document, "외부 사진", detail.getExtPhoto());
            addImageIfExists(document, "외부 편집본", detail.getExtEditPhoto());
            addImageIfExists(document, "내부 사진", detail.getIntPhoto());
            addImageIfExists(document, "내부 편집본", detail.getIntEditPhoto());

            document.close();
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

    private static void addImageIfExists(Document document, String label, String path) throws Exception {
        if (path == null || path.isBlank()) {
            document.add(new Paragraph(label + " : 이미지 없음"));
            return;
        }
        File file = new File(path);
        if (file.exists()) {
            document.add(new Paragraph(label));
            Image img = Image.getInstance(file.getAbsolutePath());
            img.scaleToFit(400, 300);
            img.setSpacingAfter(10);
            document.add(img);
        } else {
            document.add(new Paragraph(label + " : 이미지 없음"));
        }
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
