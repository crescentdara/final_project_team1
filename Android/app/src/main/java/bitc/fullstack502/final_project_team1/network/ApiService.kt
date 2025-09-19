package bitc.fullstack502.final_project_team1.network

import bitc.fullstack502.final_project_team1.network.dto.AppUserSurveyStatusResponse
import bitc.fullstack502.final_project_team1.network.dto.AssignedBuilding
import bitc.fullstack502.final_project_team1.network.dto.BuildingDetailDto
import bitc.fullstack502.final_project_team1.network.dto.ListWithStatusResponse
import bitc.fullstack502.final_project_team1.network.dto.LoginRequest
import bitc.fullstack502.final_project_team1.network.dto.LoginResponse
import bitc.fullstack502.final_project_team1.network.dto.SurveyListItemDto
import bitc.fullstack502.final_project_team1.network.dto.SurveyResultResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
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
    suspend fun getAssigned(@Query("userId") userId: Int): List<AssignedBuilding>

    @GET("assigned/nearby")
    suspend fun getAssignedNearby(
        @Query("userId") userId: Int,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radiusKm") radiusKm: Double
    ): List<AssignedBuilding>

    @GET("building/detail")
    suspend fun getBuildingDetail(@Query("buildingId") buildingId: Long): BuildingDetailDto

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

    @GET("survey/result/{id}")
    suspend fun getSurvey(@Path("id") id: Long): Response<SurveyResultResponse>

    /** 상단 카운트 (서버: GET /app/surveys/status) */
    @GET("surveys/status")
    suspend fun getSurveyStatus(
        @Header("Authorization") token: String
    ): AppUserSurveyStatusResponse

    /** 목록 + 카운트 (status = APPROVED|SENT|REJECTED|TEMP|null) */
    @GET("surveys")
    suspend fun getSurveys(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): ListWithStatusResponse<SurveyListItemDto>

    /** 재조사 목록 + 카운트 (status=REJECTED 고정) */
    @GET("surveys")
    suspend fun getSurveysReJe(
        @Header("Authorization") token: String,
        @Query("status") status: String = "REJECTED",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): ListWithStatusResponse<SurveyListItemDto>

    /** 재조사 시작(TEMP 전환) (서버: POST /app/surveys/reinspect/{id}/redo/start) */
    @POST("surveys/reinspect/{surveyId}/redo/start")
    suspend fun startRedo(
        @Header("Authorization") token: String,
        @Path("surveyId") surveyId: Long
    ): ResponseBody
}
