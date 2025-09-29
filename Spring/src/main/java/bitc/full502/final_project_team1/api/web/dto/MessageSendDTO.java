package bitc.full502.final_project_team1.api.web.dto;

import lombok.Getter;
import lombok.Setter;

// 관리자/결재자가 메시지를 보낼 때 사용하는 DTO
@Getter
@Setter
public class MessageSendDTO {
    private Long senderId;     // 관리자/결재자 ID
    private Long receiverId;   // 조사자 ID (전체 발송 시 null)
    private String title;
    private String content;
}
