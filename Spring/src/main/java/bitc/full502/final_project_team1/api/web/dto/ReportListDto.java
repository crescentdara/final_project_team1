package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.core.domain.entity.ReportEntity;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReportListDto {
    private Long id;             // ReportEntity ID
    private String caseNo;       // 관리번호 (M-{surveyResult.id})
    private String investigator; // 조사원 이름/아이디
    private String address;      // 건물 주소
    private String pdfPath;      // PDF 경로
    private String createdAt;

    // ReportListDto.java
    public static ReportListDto fromEntity(ReportEntity e) {
        var survey = e.getSurveyResult();
        var user = (survey != null ? survey.getUser() : null);
        var building = (survey != null ? survey.getBuilding() : null);

        return ReportListDto.builder()
            .id(e.getId())
            .caseNo(survey == null ? ("R-" + e.getId()) : ("M-" + survey.getId()))
            .investigator(user == null ? "조사원 없음"
                : (user.getName() != null ? user.getName() : user.getUsername()))
            .address(building == null ? "주소 없음" : building.getLotAddress())
            .pdfPath(e.getPdfPath())
            .createdAt(
                e.getCreatedAt() == null
                    ? null
                    : e.getCreatedAt().toString()
            )
            .build();
    }

}
