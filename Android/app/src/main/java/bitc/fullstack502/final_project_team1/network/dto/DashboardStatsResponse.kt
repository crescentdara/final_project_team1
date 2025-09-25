package bitc.fullstack502.final_project_team1.network.dto

data class DashboardStatsResponse(
    val progressRate: Double,
    val total: Long,
    val todayComplete: Long,
    val inProgress: Long,
    val waitingApproval: Long,
    val approved: Long
)

