package bitc.full502.final_project_team1.api.app.controller;

import bitc.full502.final_project_team1.api.web.dto.MessageReadDTO;
import bitc.full502.final_project_team1.api.web.dto.MessageResponseDTO;
import bitc.full502.final_project_team1.core.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 앱용 메시지 API 컨트롤러
 * - 메시지 보관함 리스트 조회
 * - 미읽음 메시지 개수 조회
 * - 메시지 읽음 처리
 */
@RestController
@RequestMapping("/app/messages")
@RequiredArgsConstructor
public class AppMessageController {

    private final MessageService messageService;

    /**
     * 메시지 보관함 리스트 조회 (개인 + 단체 메시지)
     * @param userId 조사원 ID
     * @return 메시지 리스트 (단체/개인 구분 포함)
     */
    @GetMapping("/list")
    public ResponseEntity<List<MessageResponseDTO>> getMessageList(
            @RequestParam("userId") Long userId
    ) {
        List<MessageResponseDTO> messages = messageService.getMessagesForUser(userId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 미읽음 메시지 개수 조회
     * @param userId 조사원 ID
     * @return 미읽음 개수
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestParam("userId") Long userId
    ) {
        // 전체 메시지 조회 후 읽지 않은 것만 카운트
        List<MessageResponseDTO> messages = messageService.getMessagesForUser(userId);
        long unreadCount = messages.stream()
                .filter(msg -> !msg.isReadFlag())
                .count();

        Map<String, Long> result = new HashMap<>();
        result.put("unreadCount", unreadCount);
        return ResponseEntity.ok(result);
    }

    /**
     * 메시지 읽음 처리
     * @param dto messageId 포함
     * @return 성공 메시지
     */
    @PatchMapping("/read")
    public ResponseEntity<String> markAsRead(@RequestBody MessageReadDTO dto) {
        messageService.markAsRead(dto);
        return ResponseEntity.ok("읽음 처리 완료");
    }
}
