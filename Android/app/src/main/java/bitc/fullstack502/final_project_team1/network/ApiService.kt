package bitc.fullstack502.final_project_team1.network

import bitc.fullstack502.final_project_team1.network.dto.AppUserSurveyStatusResponse
import bitc.fullstack502.final_project_team1.network.dto.AssignedBuilding
import bitc.fullstack502.final_project_team1.network.dto.BuildingDetailDto
import bitc.fullstack502.final_project_team1.network.dto.DashboardStatsResponse
import bitc.fullstack502.final_project_team1.network.dto.ListWithStatusResponse
import bitc.fullstack502.final_project_team1.network.dto.LoginRequest
import bitc.fullstack502.final_project_team1.network.dto.LoginResponse
import bitc.fullstack502.final_project_team1.network.dto.PageResponse
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

    // 배정/건물
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

    // 상세/최근 (헤더 필수)
    @GET("survey/result/{id}")
    suspend fun getSurveyDetail(
        @Header("X-USER-ID") userId: Long,
        @Path("id") id: Long
    ): SurveyResultDetailDto

    @GET("survey/result/latest")
    suspend fun getSurveyLatest(
        @Header("X-USER-ID") userId: Long,
        @Query("buildingId") buildingId: Long
    ): SurveyResultDetailDto?

//    @GET("surveys/{id}")
//    suspend fun getSurveyDetail(
//        @Header("X-USER-ID") userId: Long,
//        @Path("id") id: Long
//    ): SurveyResultDetailDto
//
//    @GET("surveys/latest")
//    suspend fun getSurveyLatest(
//        @Header("X-USER-ID") userId: Long,
//        @Query("buildingId") buildingId: Long
//    ): SurveyResultDetailDto?

    // 현황/목록 (서버: /app/survey/status..., 클라: /survey/status...)
    @GET("survey/status/status")
    suspend fun getSurveyStatus(
        @Header("X-USER-ID") userId: Long
    ): AppUserSurveyStatusResponse

    @GET("survey/status")
    suspend fun getSurveys(
        @Header("X-USER-ID") userId: Long,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): ListWithStatusResponse<SurveyListItemDto>

    @GET("survey/status")
    suspend fun getSurveysReJe(
        @Header("X-USER-ID") userId: Long,
        @Query("status") status: String = "REJECTED",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): ListWithStatusResponse<SurveyListItemDto>

    @POST("survey/reinspect/{surveyId}/redo/start")
    suspend fun startRedo(
        @Header("X-USER-ID") userId: Long,
        @Path("surveyId") surveyId: Long
    ): ResponseBody



    // 조사 거절 API 추가
//    @POST("assigned/reject")
//    suspend fun rejectAssignment(
//        @Query("buildingId") buildingId: Long
//    ): Response<Void>

    @POST("assigned/reject")
    suspend fun rejectAssignment(
        @Header("X-USER-ID") userId: Long,
        @Query("buildingId") buildingId: Long
    ): retrofit2.Response<Void>



    @GET("survey/result/list")
    suspend fun getSurveyResults(
        @Header("X-USER-ID") userId: Long,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<SurveyResultResponse>>

    // ✅ 앱 메인 대시보드 통계 조회
    @GET("dashboard/stats")
    suspend fun getDashboardStats(
        @Header("X-USER-ID") userId: Long,
        @Header("X-AUTH-TOKEN") token: String
    ): DashboardStatsResponse

}

