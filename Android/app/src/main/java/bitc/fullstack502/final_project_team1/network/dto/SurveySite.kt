package bitc.fullstack502.final_project_team1.network.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SurveySite(
    val id: Long,
    val name: String,
    val lat: Double,
    val lng: Double
) : Parcelable

data class AppUserSurveyStatusResponse(
    val approved: Long,
    val rejected: Long,
    val sent: Long,
    val temp: Long
)

data class SurveyListItemDto(
    val surveyId: Long,
    val buildingId: Long,
    val address: String?,
    val buildingName: String?,
    val status: String,
    val rejectReason: String?,
    val assignedAtIso: String?,
    val latitude: Double?,
    val longitude: Double?
)

data class PageDto<T>(
    val content: List<T>,
    val number: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
)

data class ListWithStatusResponse<T>(
    val status: AppUserSurveyStatusResponse,
    val page: PageDto<T>
)
