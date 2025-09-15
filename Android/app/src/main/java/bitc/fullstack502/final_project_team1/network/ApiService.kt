import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("test")
    suspend fun getHello(): Response<String>
}
