package bitc.full502.final_project_team1.api.web.dto;

import bitc.full502.final_project_team1.core.domain.entity.MessageEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageResponseDTO {
    private Long messageId;
    private Long senderId;
    private String senderName;   // 관리자/결재자 이름
    private Long receiverId;     // 추가
    private String receiverName; // 추가
    private String title;
    private String content;
    private LocalDateTime sentAt;
    private boolean readFlag;

    // 변환 메서드 (Entity → DTO)
    public static MessageResponseDTO fromEntity(MessageEntity entity) {
        return MessageResponseDTO.builder()
                .messageId(entity.getId())
                .senderId(entity.getSender().getUserId())
                .senderName(entity.getSender().getName())
                .receiverId(entity.getReceiver() != null ? entity.getReceiver().getUserId() : null)
                .receiverName(entity.getReceiver() != null ? entity.getReceiver().getName() : "전체")
                .title(entity.getTitle())
                .content(entity.getContent())
                .sentAt(entity.getSentAt())
                .readFlag(entity.isReadFlag())
                .build();
    }
}
