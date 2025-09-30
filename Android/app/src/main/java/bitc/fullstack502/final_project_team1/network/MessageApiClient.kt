package bitc.fullstack502.final_project_team1.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import kotlin.jvm.java

/**
 * 메시지 API 클라이언트 (별도 인스턴스)
 * - 기존 ApiClient와 독립적으로 동작
 */
object MessageApiClient {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/app/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: MessageApiService = retrofit.create(MessageApiService::class.java)
}