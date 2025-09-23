package bitc.fullstack502.final_project_team1.ui.main

import android.content.Context
import bitc.fullstack502.final_project_team1.core.AuthManager
import bitc.fullstack502.final_project_team1.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// 한글 주석: 활동현황 결과 DTO
data class ActivityStats(
    val progressPercent: Int,   // 진행률 %
    val totalCount: Int,        // 총 건수
    val todayCompleted: Int     // 금일 완료 건 (상태 합계: SENT 기준)
)

object ActivityStatsLoader {

    // 한글 주석: 진행률/총건수/금일완료를 X-USER-ID 기반으로 계산
    suspend fun fetch(context: Context): ActivityStats = withContext(Dispatchers.IO) {
        val userId: Int = AuthManager.userIdOrThrow(context)

        // 1) 상태 집계 (approved/rejected/sent/temp)
        val status = ApiClient.service.getSurveyStatus(userId)

        // 2) 총 건수: status=null, size=1로 호출하여 page.totalElements 이용
        val totalPage = ApiClient.service.getSurveys(
            userId = userId,
            status = null,
            page = 0,
            size = 1
        ).page
        val totalCount = totalPage.totalElements.toInt()

        // 3) 금일 완료: SENT 기준으로 페이지 순회하며 updatedAtIso가 오늘(YYYY-MM-DD)인 것만 카운트
        val todayPrefix = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var todayCompleted = 0
        var page = 0
        val pageSize = 100
        while (true) {
            val res = ApiClient.service.getSurveys(
                userId = userId,
                status = "SENT",
                page = page,
                size = pageSize
            )
            val items = res.page.content
            if (items.isEmpty()) break
            todayCompleted += items.count { it.updatedAtIso?.startsWith(todayPrefix) == true }
            if (res.page.last) break
            page++
        }

        // 4) 진행률 계산 (상태 합계 기준): (sent + approved) / (approved+rejected+sent+temp)
        val progressed = (status.sent + status.approved).toDouble()
        val denominator = (status.approved + status.rejected + status.sent + status.temp)
            .coerceAtLeast(1L).toDouble()
        val progressPercent = (progressed * 100.0 / denominator).roundToInt()

        ActivityStats(
            progressPercent = progressPercent,
            totalCount = totalCount,
            todayCompleted = todayCompleted
        )
    }
}


