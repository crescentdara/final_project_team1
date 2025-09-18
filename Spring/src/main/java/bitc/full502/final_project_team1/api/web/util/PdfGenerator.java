package bitc.full502.final_project_team1.api.web.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;

import java.io.File;
import java.io.FileOutputStream;

public class PdfGenerator {

    public static String generateSurveyReport(SurveyResultEntity surveyResult) throws Exception {

        String basePath = "reports/";
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs(); // ✅ 폴더 없으면 자동 생성
        }

        String fileName = basePath + "report-" + surveyResult.getId() + ".pdf";

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        // 제목
        document.add(new Paragraph("건축물 현장조사 결과 보고서", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        document.add(new Paragraph(" ")); // 빈 줄

        // 기본 정보
        document.add(new Paragraph("건물명: " + surveyResult.getBuilding().getBuildingName()));
        document.add(new Paragraph("조사자: " + surveyResult.getUser().getName()));
        document.add(new Paragraph("상태: " + surveyResult.getStatus()));
        document.add(new Paragraph(" "));

        // 상세 내용 테이블
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        table.addCell("조사불가 여부");
        table.addCell(String.valueOf(surveyResult.getPossible()));

        table.addCell("행정목적 활용 여부");
        table.addCell(String.valueOf(surveyResult.getAdminUse()));

        table.addCell("유휴비율");
        table.addCell(String.valueOf(surveyResult.getIdleRate()));

        table.addCell("안전등급");
        table.addCell(String.valueOf(surveyResult.getSafety()));

        table.addCell("외부상태 - 외벽");
        table.addCell(String.valueOf(surveyResult.getWall()));

        table.addCell("외부상태 - 옥상");
        table.addCell(String.valueOf(surveyResult.getRoof()));

        table.addCell("외부상태 - 창호");
        table.addCell(String.valueOf(surveyResult.getWindowState()));

        table.addCell("외부상태 - 주차 가능 여부");
        table.addCell(String.valueOf(surveyResult.getParking()));

        table.addCell("내부상태 - 현관");
        table.addCell(String.valueOf(surveyResult.getEntrance()));

        table.addCell("내부상태 - 천장");
        table.addCell(String.valueOf(surveyResult.getCeiling()));

        table.addCell("내부상태 - 바닥");
        table.addCell(String.valueOf(surveyResult.getFloor()));

        table.addCell("외부 기타사항");
        table.addCell(surveyResult.getExtEtc() != null ? surveyResult.getExtEtc() : "-");

        table.addCell("내부 기타사항");
        table.addCell(surveyResult.getIntEtc() != null ? surveyResult.getIntEtc() : "-");

        document.add(table);

        document.close();

        return fileName;
    }
}
