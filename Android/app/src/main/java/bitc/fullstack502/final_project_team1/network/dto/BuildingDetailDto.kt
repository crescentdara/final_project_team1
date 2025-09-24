package bitc.fullstack502.final_project_team1.network.dto

data class BuildingDetailDto(
    val id: Long,
    val lotAddress: String?,
    val buildingName: String?,
    val groundFloors: Int?,
    val basementFloors: Int?,
    val totalFloorArea: Double?,
    val landArea: Double?,
    val mainUseCode: String?,
    val mainUseName: String?,
    val etcUse: String?,
    val structureName: String?,
    val height: Double?
)
