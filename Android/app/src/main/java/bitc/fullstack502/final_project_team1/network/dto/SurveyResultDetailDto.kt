package bitc.fullstack502.final_project_team1.network.dto

data class SurveyResultDetailDto(
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
    val buildingId: Long?,
    val userId: Long?,
    val createdAt: String?,
    val updatedAt: String?
)
