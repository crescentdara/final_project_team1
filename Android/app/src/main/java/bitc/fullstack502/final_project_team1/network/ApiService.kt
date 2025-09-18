package bitc.fullstack502.final_project_team1.network

import bitc.fullstack502.final_project_team1.network.dto.AssignedBuilding
import bitc.fullstack502.final_project_team1.network.dto.BuildingDetailDto
import bitc.fullstack502.final_project_team1.network.dto.LoginRequest
import bitc.fullstack502.final_project_team1.network.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("assigned")
    suspend fun getAssigned(
        @Query("userId") userId: Int
    ): List<AssignedBuilding>

    @GET("assigned/nearby")
    suspend fun getAssignedNearby(
        @Query("userId") userId: Int,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radiusKm") radiusKm: Double
    ): List<AssignedBuilding>

    @GET("building/detail")
    suspend fun getBuildingDetail(
        @Query("buildingId") buildingId: Long
    ): BuildingDetailDto


}
