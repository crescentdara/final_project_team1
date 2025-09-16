package bitc.fullstack502.final_project_team1.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object ApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/app/")
        .addConverterFactory(ScalarsConverterFactory.create()) // String 응답 처리
        .addConverterFactory(GsonConverterFactory.create()) // JSON 응답 처리
        .build()

    val service: ApiService = retrofit.create(ApiService::class.java)
    
    // 실제 서버 URL로 변경할 때 사용
    // .baseUrl("https://your-server.com/api/")
}
