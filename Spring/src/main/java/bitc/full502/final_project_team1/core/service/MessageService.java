package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.web.dto.MessageSendDTO;
import bitc.full502.final_project_team1.api.web.dto.MessageResponseDTO;
import bitc.full502.final_project_team1.api.web.dto.MessageReadDTO;
import bitc.full502.final_project_team1.api.web.dto.UserSimpleDto;

import java.util.List;

public interface MessageService {

    /** 메시지 전송 (저장 + 푸시 알림) */
    void sendMessage(MessageSendDTO dto);

    /** 특정 유저의 메시지 조회 (개인 + 전체 발송 포함) */
    List<MessageResponseDTO> getMessagesForUser(Long userId);

    /** 메시지 읽음 처리 */
    void markAsRead(MessageReadDTO dto);

    /** 보낸 메세지 조회 **/
    List<MessageResponseDTO> getMessagesSentByUser(Long senderId);

    /** 특정 발신자가 보낸 메시지 중 수신자/키워드 조건 필터링 **/
    List<MessageResponseDTO> searchMessages(Long senderId, Long receiverId, String keyword);

    /** 조사원 리스트 **/
    List<UserSimpleDto> getUsersByRole(String roleName);
}
