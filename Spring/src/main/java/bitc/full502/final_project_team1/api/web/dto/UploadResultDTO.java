package bitc.full502.final_project_team1.api.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResultDTO {
    private int successCount;          // 성공 건수
    private int failCount;             // 실패 건수
    private List<String> failMessages; // 실패 메시지 리스트
}
