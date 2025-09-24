package bitc.fullstack502.final_project_team1.network

import bitc.fullstack502.final_project_team1.network.dto.AppUserSurveyStatusResponse
import bitc.fullstack502.final_project_team1.network.dto.AssignedBuilding
import bitc.fullstack502.final_project_team1.network.dto.BuildingDetailDto
import bitc.fullstack502.final_project_team1.network.dto.ListWithStatusResponse
import bitc.fullstack502.final_project_team1.network.dto.LoginRequest
import bitc.fullstack502.final_project_team1.network.dto.LoginResponse
import bitc.fullstack502.final_project_team1.network.dto.SurveyListItemDto
import bitc.fullstack502.final_project_team1.network.dto.SurveyResultDetailDto
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


// ApiService.kt
interface ApiService {

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("assigned")
    suspend fun getAssigned(@Query("userId") userId: Long): List<AssignedBuilding>

    @GET("assigned/nearby")
    suspend fun getAssignedNearby(
        @Query("userId") userId: Long,
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

    @GET("survey/result/{id}")
    suspend fun getSurvey(@Path("id") id: Long): Response<SurveyResultResponse>


    // ===== 여기부터 목록/카운트 경로 교체 =====
    /** 상단 카운트 (서버: GET /app/survey/status/status, Header: X-USER-ID) */
    @GET("survey/status/status")
    suspend fun getSurveyStatus(
        @Header("X-USER-ID") userId: Long
    ): AppUserSurveyStatusResponse

    /** 목록 + 카운트 (서버: GET /app/survey/status, Header: X-USER-ID, status optional) */
    @GET("survey/status")
    suspend fun getSurveys(
        @Header("X-USER-ID") userId: Long,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): ListWithStatusResponse<SurveyListItemDto>

    /** 재조사 목록 (status=REJECTED 고정) */
    @GET("survey/status")
    suspend fun getSurveysReJe(
        @Header("X-USER-ID") userId: Long,
        @Query("status") status: String = "REJECTED",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): ListWithStatusResponse<SurveyListItemDto>

    /** 재조사 시작 경로는 서버 구현에 맞춰 조정 필요 */
    @POST("survey/reinspect/{surveyId}/redo/start")
    suspend fun startRedo(
        @Header("X-USER-ID") userId: Long,
        @Path("surveyId") surveyId: Long
    ): ResponseBody

    // network/ApiService.kt (추가)
    @GET("surveys/{id}")
    suspend fun getSurveyDetail(
        @Header("X-USER-ID") userId: Long,
        @Path("id") id: Long
    ): SurveyResultDetailDto

    @GET("surveys/latest")
    suspend fun getSurveyLatest(
        @Header("X-USER-ID") userId: Long,
        @Query("buildingId") buildingId: Long
    ): SurveyResultDetailDto?

}
