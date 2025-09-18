package bitc.full502.final_project_team1.api.web.util;

import bitc.full502.final_project_team1.api.web.dto.ResultDetailDto;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class PdfGenerator {

    public static String generateSurveyReport(ResultDetailDto detail, UserAccountEntity approver) {
        try {
            // 저장 경로 설정
            String basePath = "reports/";
            File dir = new File(basePath);
            if (!dir.exists()) dir.mkdirs();

            String fileName = basePath + "report-" + detail.getId() + ".pdf";

            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // 🔹 제목
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, Color.BLACK);
            Paragraph title = new Paragraph("건축물 현장조사 보고서", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 🔹 기본 정보
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15);

            addCell(infoTable, "사례 번호", "M-" + detail.getId());
            addCell(infoTable, "조사자", detail.getInvestigator());
            addCell(infoTable, "주소", detail.getAddress());
            addCell(infoTable, "승인자", approver.getName() + " (" + approver.getUsername() + ")");
            addCell(infoTable, "승인일시", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(java.time.LocalDateTime.now()));

            document.add(infoTable);

            // 🔹 점검 항목
            Paragraph sectionTitle = new Paragraph("점검 항목", new Font(Font.HELVETICA, 14, Font.BOLD));
            sectionTitle.setSpacingAfter(10);
            document.add(sectionTitle);

            PdfPTable checkTable = new PdfPTable(2);
            checkTable.setWidthPercentage(100);

            addCell(checkTable, "조사 가능 여부", mapPossible(detail.getPossible()));
            addCell(checkTable, "행정 목적 활용", mapAdminUse(detail.getAdminUse()));
            addCell(checkTable, "유휴 비율", mapIdleRate(detail.getIdleRate()));
            addCell(checkTable, "안전 등급", mapSafety(detail.getSafety()));
            addCell(checkTable, "외벽 상태", mapState(detail.getWall()));
            addCell(checkTable, "옥상 상태", mapState(detail.getRoof()));
            addCell(checkTable, "창호 상태", mapState(detail.getWindowState()));
            addCell(checkTable, "주차 가능", mapState(detail.getParking()));
            addCell(checkTable, "현관 상태", mapState(detail.getEntrance()));
            addCell(checkTable, "천장 상태", mapState(detail.getCeiling()));
            addCell(checkTable, "바닥 상태", mapState(detail.getFloor()));

            document.add(checkTable);

            // 🔹 사진 (있을 경우만 추가)
            if (detail.getExtPhoto() != null) {
                document.add(new Paragraph("외부 사진"));
                Image img = Image.getInstance(detail.getExtPhoto());
                img.scaleToFit(400, 300);
                document.add(img);
            }

            if (detail.getIntPhoto() != null) {
                document.add(new Paragraph("내부 사진"));
                Image img = Image.getInstance(detail.getIntPhoto());
                img.scaleToFit(400, 300);
                document.add(img);
            }

            document.close();
            return fileName;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("PDF 생성 실패", e);
        }
    }

    // ================= 헬퍼 메서드 =================

    private static void addCell(PdfPTable table, String key, String value) {
        PdfPCell cell1 = new PdfPCell(new Phrase(key));
        PdfPCell cell2 = new PdfPCell(new Phrase(value != null ? value : "-"));
        cell1.setBackgroundColor(new Color(230, 230, 250));
        cell1.setPadding(5);
        cell2.setPadding(5);
        table.addCell(cell1);
        table.addCell(cell2);
    }

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
            default -> "-";
        };
    }

    private static String mapState(Integer v) {
        return v == null ? "-" : (v == 1 ? "양호" : v == 2 ? "보통" : v == 3 ? "불량" : "-");
    }
}
