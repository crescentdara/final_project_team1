package bitc.fullstack502.final_project_team1.network

import bitc.fullstack502.final_project_team1.network.dto.MessageDto
import bitc.fullstack502.final_project_team1.network.dto.MessageReadRequest
import bitc.fullstack502.final_project_team1.network.dto.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * 메시지 API 서비스 (앱 전용)
 * - 기존 ApiService와 분리하여 새로 생성
 */
interface MessageApiService {

    /**
     * 메시지 보관함 리스트 조회
     * @param userId 조사원 ID
     */
    @GET("messages/list")
    suspend fun getMessageList(
        @Query("userId") userId: Long
    ): Response<List<MessageDto>>

    /**
     * 미읽음 메시지 개수 조회
     * @param userId 조사원 ID
     */
    @GET("messages/unread-count")
    suspend fun getUnreadCount(
        @Query("userId") userId: Long
    ): Response<UnreadCountResponse>

    /**
     * 메시지 읽음 처리
     * @param request messageId, userId 포함
     */
    @PATCH("messages/read")
    suspend fun markAsRead(
        @Body request: MessageReadRequest
    ): Response<String>
}
