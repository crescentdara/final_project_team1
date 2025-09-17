package bitc.fullstack502.final_project_team1.network.dto

fun AssignedBuilding.toSurveySiteOrNull(): SurveySite? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    if (lat !in -90.0..90.0 || lng !in -180.0..180.0) return null
    return SurveySite(
        id = id,
        name = lotAddress.ifBlank { "조사지 #$id" },
        lat = lat,
        lng = lng
    )
}
fun List<AssignedBuilding>.toSurveySites(): ArrayList<SurveySite> =
    mapNotNull { it.toSurveySiteOrNull() }.let { ArrayList(it) }
