package bitc.fullstack502.final_project_team1.network.dto

data class SurveyResultRequest(
    val surveyId: Long? = null,
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
    val extPhoto: String? = null,
    val extEditPhoto: String? = null,
    val intPhoto: String? = null,
    val intEditPhoto: String? = null,
    val status: String,            // "TEMP" | "SENT"
    val buildingId: Long,
    val userId: Long
)


