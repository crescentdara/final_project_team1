// bitc.fullstack502.final_project_team1.network.ApiClient
package bitc.fullstack502.final_project_team1.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

// bitc.fullstack502.final_project_team1.network.ApiClient
object ApiClient {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/app/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: ApiService = retrofit.create(ApiService::class.java)

    /** 호스트 루트 (예: http://10.0.2.2:8080) */
    fun originBaseUrl(): String =
        retrofit.baseUrl().newBuilder().encodedPath("/").build().toString().trimEnd('/')
}

