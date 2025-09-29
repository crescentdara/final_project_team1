package bitc.full502.final_project_team1.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    /** 발신자 (관리자/결재자) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserAccountEntity sender;

    /** 수신자 (조사자). 전체 전송의 경우 NULL */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private UserAccountEntity receiver;

    /** 메시지 제목 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 메시지 본문 */
    @Column(nullable = false, length = 1000)
    private String content;

    /** 보낸 시간 */
    @Column(nullable = false)
    private LocalDateTime sentAt;

    /** 읽음 여부 */
    @Column(nullable = false)
    private boolean readFlag;
}
