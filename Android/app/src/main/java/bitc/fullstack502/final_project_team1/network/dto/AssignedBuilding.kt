package bitc.fullstack502.final_project_team1.network.dto

data class AssignedBuilding(
    val id: Long,
    val lotAddress: String,
    val latitude: Double?,
    val longitude: Double?,
    val distanceMeters: Double?
)
