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