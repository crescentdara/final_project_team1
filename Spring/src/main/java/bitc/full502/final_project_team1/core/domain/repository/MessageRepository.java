package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.MessageEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    /** 특정 수신자의 메시지 목록 조회 */
    List<MessageEntity> findByReceiverOrderBySentAtDesc(UserAccountEntity receiver);

    /** 전체 발송 메시지(=receiver가 NULL인 경우) 조회 */
    List<MessageEntity> findByReceiverIsNullOrderBySentAtDesc();

    /** 읽지 않은 메시지 목록 조회 */
    List<MessageEntity> findByReceiverAndReadFlagFalse(UserAccountEntity receiver);

    /**  특정 발신자가 보낸 메시지 조회 */
    List<MessageEntity> findBySenderOrderBySentAtDesc(UserAccountEntity sender);

    /** 특정 발신자가 보낸 메시지 중 수신자/키워드 조건 필터링 */
    @Query("SELECT m FROM MessageEntity m " +
            "WHERE m.sender = :sender " +
            "AND (:receiver IS NULL OR m.receiver = :receiver) " +
            "AND (:keyword IS NULL OR m.title LIKE %:keyword% OR m.content LIKE %:keyword%) " +
            "ORDER BY m.sentAt DESC")
    List<MessageEntity> searchMessages(
            @Param("sender") UserAccountEntity sender,
            @Param("receiver") UserAccountEntity receiver,
            @Param("keyword") String keyword
    );

}
