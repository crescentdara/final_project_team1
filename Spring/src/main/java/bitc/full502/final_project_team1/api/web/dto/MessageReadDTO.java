package bitc.full502.final_project_team1.api.web.dto;

import lombok.Getter;
import lombok.Setter;

// 조사자가 메시지를 읽었을 때 전달
@Getter
@Setter
public class MessageReadDTO {
    private Long messageId;
    private Long userId;   // 읽은 사람 ID (조사자)
}
