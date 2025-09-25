package bitc.fullstack502.final_project_team1.network.dto

data class SurveyResultResponse(
    val surveyId: Long,
    val possible: Int?,
    val adminUse: Int?,
    val idleRate: Int?,
    val safety: Int?,
    val wall: Int?,
    val roof: Int?,
    val windowState: Int?,
    val parking: Int?,
    val entrance: Int?,
    val ceiling: Int?,
    val floor: Int?,
    val extEtc: String?,
    val intEtc: String?,
    val extPhoto: String?,
    val extEditPhoto: String?,
    val intPhoto: String?,
    val intEditPhoto: String?,
    val status: String?,

    // 새로 추가
    val buildingId: Long?,
    val buildingAddress: String?,
    val userId: Long?,

    val createdAt: String?,
    val updatedAt: String?
)
