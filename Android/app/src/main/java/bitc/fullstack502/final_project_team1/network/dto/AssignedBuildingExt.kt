package bitc.fullstack502.final_project_team1.network.dto

fun AssignedBuilding.toSurveySiteOrNull(): SurveySite? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    if (lat !in -90.0..90.0 || lng !in -180.0..180.0) return null

    // String? 에 안전하게 이름 생성
    val name = lotAddress?.takeIf { it.isNotBlank() } ?: "조사지 #$id"

    return SurveySite(
        id = id,
        name = name,
        lat = lat,
        lng = lng
    )
}

fun List<AssignedBuilding>.toSurveySites(): ArrayList<SurveySite> =
    ArrayList(mapNotNull { it.toSurveySiteOrNull() })
