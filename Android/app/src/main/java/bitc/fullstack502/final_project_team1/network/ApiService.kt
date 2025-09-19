package bitc.fullstack502.final_project_team1.network

import bitc.fullstack502.final_project_team1.network.dto.AssignedBuilding
import bitc.fullstack502.final_project_team1.network.dto.BuildingDetailDto
import bitc.fullstack502.final_project_team1.network.dto.LoginRequest
import bitc.fullstack502.final_project_team1.network.dto.LoginResponse
import bitc.fullstack502.final_project_team1.network.dto.SurveyResultResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
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

    @Multipart
    @POST("survey/result/submit")
    suspend fun submitSurvey(
        @Part("dto") dto: RequestBody,
        @Part extPhoto: MultipartBody.Part?,
        @Part extEditPhoto: MultipartBody.Part?,
        @Part intPhoto: MultipartBody.Part?,
        @Part intEditPhoto: MultipartBody.Part?
    ): Response<SurveyResultResponse>

    @Multipart
    @POST("survey/result/save-temp")
    suspend fun saveTemp(
        @Part("dto") dto: RequestBody,
        @Part extPhoto: MultipartBody.Part?,
        @Part extEditPhoto: MultipartBody.Part?,
        @Part intPhoto: MultipartBody.Part?,
        @Part intEditPhoto: MultipartBody.Part?
    ): Response<SurveyResultResponse>

    // (옵션) 수정 => PUT /app/survey/result/edit/{id}
    @Multipart
    @PUT("survey/result/edit/{id}")
    suspend fun updateSurvey(
        @Path("id") id: Long,
        @Part("dto") dto: RequestBody,
        @Part extPhoto: MultipartBody.Part?,
        @Part extEditPhoto: MultipartBody.Part?,
        @Part intPhoto: MultipartBody.Part?,
        @Part intEditPhoto: MultipartBody.Part?
    ): Response<SurveyResultResponse>

    // (옵션) 단건 조회
    @GET("survey/result/{id}")
    suspend fun getSurvey(@Path("id") id: Long): Response<SurveyResultResponse>


}
