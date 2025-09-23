package bitc.full502.final_project_team1.api.web.controller;

import bitc.full502.final_project_team1.api.web.dto.MessageSendDTO;
import bitc.full502.final_project_team1.api.web.dto.MessageResponseDTO;
import bitc.full502.final_project_team1.api.web.dto.MessageReadDTO;
import bitc.full502.final_project_team1.api.web.dto.UserSimpleDto;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.enums.Role;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.core.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/web/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserAccountRepository userAccountRepository;

    /** 리액트에서 조사원 목록 불러올 때 호출하는 API */
    @GetMapping
    public ResponseEntity<List<UserSimpleDto>> getUsersByRole(@RequestParam String role) {
        Role enumRole = Role.valueOf(role.toUpperCase());
        List<UserAccountEntity> users = userAccountRepository.findByRole(enumRole);
        return ResponseEntity.ok(
                users.stream()
                        .map(UserSimpleDto::from) // UserSimpleDto로 변환
                        .toList()
        );
    }


    /** 메시지 전송 (관리자/결재자 → 조사자/전체) */
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody MessageSendDTO dto) {
        messageService.sendMessage(dto);
        return ResponseEntity.ok("메시지 전송 완료");
    }

    /** 특정 유저 메시지 조회 */
    @GetMapping("/{userId}")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(@PathVariable Long userId) {
        List<MessageResponseDTO> messages = messageService.getMessagesForUser(userId);
        return ResponseEntity.ok(messages);
    }

    /** 메시지 읽음 처리 */
    @PatchMapping("/read")
    public ResponseEntity<String> markAsRead(@RequestBody MessageReadDTO dto) {
        messageService.markAsRead(dto);
        return ResponseEntity.ok("읽음 처리 완료");
    }

    /** 특정 발신자가 보낸 메시지 조회 (관리자/결재자 용) */
    @GetMapping("/sent/{senderId}")
    public ResponseEntity<List<MessageResponseDTO>> getSentMessages(@PathVariable Long senderId) {
        List<MessageResponseDTO> messages = messageService.getMessagesSentByUser(senderId);
        return ResponseEntity.ok(messages);
    }

    /** 발신자가 보낸 메시지 검색 */
    @GetMapping("/sent/{senderId}/search")
    public ResponseEntity<List<MessageResponseDTO>> searchSentMessages(
            @PathVariable Long senderId,
            @RequestParam(required = false) Long receiverId,
            @RequestParam(required = false) String keyword) {

        List<MessageResponseDTO> messages =
                messageService.searchMessages(senderId, receiverId, keyword);

        return ResponseEntity.ok(messages);
    }


}
