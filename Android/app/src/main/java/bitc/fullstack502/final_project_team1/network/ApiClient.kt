import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object ApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/api/")
        .addConverterFactory(ScalarsConverterFactory.create()) // String 응답 처리
        .build()

    val service: ApiService = retrofit.create(ApiService::class.java)
}
