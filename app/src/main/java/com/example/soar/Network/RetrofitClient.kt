package com.example.soar.Network

import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val BASE_URL = ApiConfig.BASE_URL

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
        // ✅ 민감 헤더 마스킹
        redactHeader("Authorization")
        redactHeader("Refresh-Token")
        redactHeader("New-Access-Token")
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor { chain ->
            val response = chain.proceed(chain.request())
            response.header("New-Access-Token")?.let { newToken ->
                TokenManager.saveAccessToken(newToken)
            }
            response

        }
        .authenticator(TokenAuthenticator())
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .serializeNulls() // null 값도 JSON에 포함
        .setLenient()  // JSON 파싱 오류 방지
        .setDateFormat("yyyy-MM-dd") // 날짜 포맷 설정
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES) // 스네이크 케이스로,, 모두 바꿔야함
        .create()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson)) // JSON 응답 지원
            .build()
            .create(ApiService::class.java)
    }

}

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // 이 요청은 Authorization 헤더를 붙이지 않음
        if (original.header("Skip-Auth") == "true") {
            val clean = original.newBuilder()
                .removeHeader("Skip-Auth") // 서버에 이 헤더는 보내지 않음
                .build()
            return chain.proceed(clean)
        }

        val access = TokenManager.getAccessToken()
        //val refresh = TokenManager.getRefreshToken()
        val req = chain.request().newBuilder().also { b ->
            if (!access.isNullOrEmpty()) b.addHeader("Authorization", "Bearer $access")
            //if (!refresh.isNullOrEmpty())  b.addHeader("Refresh-Token",  "Bearer $refresh")
        }.build()
        return chain.proceed(req)
    }
}


class TokenAuthenticator : Authenticator {
    companion object {
        private const val AUTH = "Authorization"
        private const val REFRESH = "Refresh-Token"
    }

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {

        if (responseCount(response) > 1) return null

        synchronized(lock) {

            val currentAccess = TokenManager.getAccessToken()
            val reqAuthHeader = response.request.header(AUTH)


            // ✅ 이미 다른 스레드가 갱신을 끝낸 경우: 새 토큰으로 재요청만 생성
            if (!currentAccess.isNullOrEmpty() && reqAuthHeader != "Bearer $currentAccess") {
                return response.request.newBuilder()
                    .header(AUTH, "Bearer $currentAccess")
                    .build()
            }

            val refreshToken = TokenManager.getRefreshToken() ?: return null

            // 리프레시 전용 클라이언트(짧은 타임아웃)
            val refreshClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                    redactHeader(AUTH)
                    redactHeader(REFRESH)
                })
                .build()

            // 리프레시 요청 (서버 규격에 맞게 헤더/바디 설정)
            val refreshReq = Request.Builder()
                .url(RetrofitClient.BASE_URL + "/api/auth/refresh")
                .addHeader(REFRESH, "Bearer $refreshToken")       // ✅ 접두사 통일
                .addHeader("Accept", "application/json")
                .post("{}".toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            return try {
                refreshClient.newCall(refreshReq).execute().use { refreshResp ->
                    if (!refreshResp.isSuccessful) {
                        // 401/403 등: 리프레시 만료 → null 반환하면 원 요청은 401로 종료됨
                        return null
                    }

                    val bodyStr = refreshResp.body?.string().orEmpty()
                    val data = JSONObject(bodyStr).optJSONObject("data")

                    val newAccess = data?.optString("accessToken").orEmpty()
                    val newRefresh = data?.optString("refreshToken").orEmpty()

                    if (newAccess.isBlank() || newRefresh.isBlank()) {
                        // 서버 응답 이상 → 갱신 실패
                        return null
                    }

                    Log.d("accessToken", "Bearer $newAccess")

                    // ✅ 저장
                    TokenManager.saveAccessToken(newAccess)
                    TokenManager.saveRefreshToken(newRefresh)

                    // ✅ 새 토큰으로 원 요청 재작성
                    response.request.newBuilder()
                        .header(AUTH, "Bearer $newAccess")
                        .build()
                }
            } catch (t: Throwable) {
                // 네트워크/파싱 예외 → 갱신 실패
                null
            }
        }
    }

    private fun responseCount(resp: Response): Int =
        resp.priorResponse?.let { 1 + responseCount(it) } ?: 1
}

