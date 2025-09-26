package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.web.dto.MessageSendDTO;
import bitc.full502.final_project_team1.api.web.dto.MessageResponseDTO;
import bitc.full502.final_project_team1.api.web.dto.MessageReadDTO;
import bitc.full502.final_project_team1.api.web.dto.UserSimpleDto;
import bitc.full502.final_project_team1.core.domain.entity.MessageEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.enums.Role;
import bitc.full502.final_project_team1.core.domain.repository.MessageRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserAccountRepository userAccountRepository;

    @Override
    public void sendMessage(MessageSendDTO dto) {
        UserAccountEntity sender = userAccountRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("발신자 없음"));

        UserAccountEntity receiver = null;
        if (dto.getReceiverId() != null) {
            receiver = userAccountRepository.findById(dto.getReceiverId())
                    .orElseThrow(() -> new IllegalArgumentException("수신자 없음"));
        }

        // DB 저장
        MessageEntity entity = MessageEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .title(dto.getTitle())
                .content(dto.getContent())
                .sentAt(LocalDateTime.now())
                .readFlag(false)
                .build();

        messageRepository.save(entity);

        // ✅ 푸시 알림 전송 (FCM)
        if (receiver != null) {
            sendPushToUser(receiver, dto.getTitle(), dto.getContent());
        } else {
            // 전체 전송: 모든 조사자 대상으로 푸시
            List<UserAccountEntity> allUsers = userAccountRepository.findAll();
            for (UserAccountEntity user : allUsers) {
                if (user.getRole().name().equals("RESEARCHER")) {
                    sendPushToUser(user, dto.getTitle(), dto.getContent());
                }
            }
        }
    }

    @Override
    public List<MessageResponseDTO> getMessagesForUser(Long userId) {
        UserAccountEntity user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        List<MessageEntity> personalMessages = messageRepository.findByReceiverOrderBySentAtDesc(user);
        List<MessageEntity> broadcastMessages = messageRepository.findByReceiverIsNullOrderBySentAtDesc();

        List<MessageResponseDTO> result = new ArrayList<>();
        for (MessageEntity m : personalMessages) {
            result.add(toDto(m));
        }
        for (MessageEntity m : broadcastMessages) {
            result.add(toDto(m));
        }

        return result;
    }

    @Override
    public void markAsRead(MessageReadDTO dto) {
        MessageEntity msg = messageRepository.findById(dto.getMessageId())
                .orElseThrow(() -> new IllegalArgumentException("메시지 없음"));

        msg.setReadFlag(true);
        messageRepository.save(msg);
    }

    private MessageResponseDTO toDto(MessageEntity entity) {
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


    /** 실제 FCM 발송 메서드 */
    private void sendPushToUser(UserAccountEntity user, String title, String content) {
        // TODO: UserAccountEntity 에 fcmToken 필드를 추가해 두어야 함!
        if (user.getFcmToken() == null) return;

        Message message = Message.builder()
                .setToken(user.getFcmToken())
                .putData("title", title)
                .putData("content", content)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 메세지 조회 **/
    @Override
    public List<MessageResponseDTO> getMessagesSentByUser(Long senderId) {
        // 1. 발신자 조회
        UserAccountEntity sender = userAccountRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("발신자 없음"));

        // 2. 해당 발신자가 보낸 메시지 전부 가져오기 (최신순)
        List<MessageEntity> sentMessages = messageRepository.findBySenderOrderBySentAtDesc(sender);

        // 3. DTO 변환
        List<MessageResponseDTO> result = new ArrayList<>();
        for (MessageEntity m : sentMessages) {
            result.add(toDto(m));
        }

        return result;
    }

    /** 특정 발신자가 보낸 메시지 중 수신자/키워드 조건 필터링 **/
    public List<MessageResponseDTO> searchMessages(Long senderId, Long receiverId, String keyword) {
        UserAccountEntity sender = userAccountRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("발신자 없음"));

        UserAccountEntity receiver = null;
        if (receiverId != null) {
            receiver = userAccountRepository.findById(receiverId)
                    .orElseThrow(() -> new IllegalArgumentException("수신자 없음"));
        }

        List<MessageEntity> messages = messageRepository.searchMessages(sender, receiver, keyword);

        return messages.stream().map(this::toDto).toList();
    }

    /** 조사원 리스트 **/
    @Override
    public List<UserSimpleDto> getUsersByRole(String roleName) {
        Role role = Role.valueOf(roleName.toUpperCase()); // "RESEARCHER" → Role.RESEARCHER
        List<UserAccountEntity> users = userAccountRepository.findByRole(role);
        return users.stream()
                .map(UserSimpleDto::from)
                .toList();
    }

}
