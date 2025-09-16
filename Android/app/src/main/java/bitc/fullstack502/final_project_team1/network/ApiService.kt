package bitc.fullstack502.final_project_team1.network

import bitc.fullstack502.final_project_team1.network.dto.LoginRequest
import bitc.fullstack502.final_project_team1.network.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("test")
    suspend fun getHello(): Response<String>
    
    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>
}
