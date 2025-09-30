package bitc.fullstack502.final_project_team1.network.dto

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

/**
 * 메시지 응답 DTO
 * - 웹(관리자/결재자)로부터 받은 메시지 정보
 */
data class MessageDto(
    @SerializedName("messageId") val messageId: Long,
    @SerializedName("senderId") val senderId: Long,
    @SerializedName("senderName") val senderName: String,
    @SerializedName("receiverId") val receiverId: Long?, // null이면 단체 메시지
    @SerializedName("receiverName") val receiverName: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("sentAt") val sentAt: String, // ISO 포맷 문자열
    @SerializedName("readFlag") val readFlag: Boolean
) {
    /** 단체 메시지 여부 (receiverId가 null이면 단체) */
    val isBroadcast: Boolean
        get() = receiverId == null
}

/**
 * 메시지 읽음 처리 요청 DTO
 */
data class MessageReadRequest(
    @SerializedName("messageId") val messageId: Long,
    @SerializedName("userId") val userId: Long
)

/**
 * 미읽음 개수 응답 DTO
 */
data class UnreadCountResponse(
    @SerializedName("unreadCount") val unreadCount: Long
)
