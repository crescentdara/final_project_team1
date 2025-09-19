package bitc.fullstack502.final_project_team1.network.dto

import com.google.gson.annotations.SerializedName

data class AssignedBuilding(
    @SerializedName("id") val id: Long,
    @SerializedName("lotAddress") val lotAddress: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("distanceMeters") val distanceMeters: Double?,
    @SerializedName("assignedAt") val assignedAt: String?
)
