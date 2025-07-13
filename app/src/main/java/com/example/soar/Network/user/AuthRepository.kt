package com.example.soar.repository

import com.example.soar.Network.ApiService
import com.example.soar.Network.RetrofitClient
import com.example.soar.Network.user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** 공통 에러 파싱 */
private fun parseError(body: okhttp3.ResponseBody?): String =
    runCatching { JSONObject(body?.string() ?: "{}").optString("message") }
        .getOrDefault("예기치 못한 오류가 발생했습니다")

class AuthRepository(
    private val api: ApiService = RetrofitClient.apiService   // DI 로 교체 가능
) {

    /** 회원가입 */
    suspend fun signUp(body: SignUpRequest): Result<SignUpResponse> = withContext(Dispatchers.IO) {
        runCatching {
            api.signUp(body).let { res ->
                when {
                    res.isSuccessful -> res.body()?.data
                        ?: error("서버 응답이 비어 있습니다.")
                    else             -> error(parseError(res.errorBody()))
                }
            }
        }
    }

    /** 이메일 OTP 발송 */
    suspend fun requestEmailOtp(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            api.sendOtp(EmailRequest(email)).let { res ->
                if (res.isSuccessful) Unit else error(parseError(res.errorBody()))
            }
        }
    }

    /** 이메일 OTP 검증 */
    suspend fun verifyEmailOtp(email: String, otp: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            api.verifyOtp(OtpRequest(email, otp)).let { res ->
                if (res.isSuccessful) Unit else error(parseError(res.errorBody()))
            }
        }
    }

    suspend fun login(email: String, pw: String): Boolean {
        // 실제 구현 시 Retrofit 호출
        delay(800)
        return email == "test@example.com" && pw == "12345678"
    }
}
