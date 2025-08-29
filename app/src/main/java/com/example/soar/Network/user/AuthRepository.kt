package com.example.soar.Network.user

import com.example.soar.Network.ApiService
import com.example.soar.Network.RetrofitClient
import com.example.soar.Network.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject

/** 공통 에러 파싱 */
private fun parseError(body: ResponseBody?): String =
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

                    else -> error(parseError(res.errorBody()))
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
    suspend fun verifyEmailOtp(email: String, otp: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                api.verifyOtp(OtpRequest(email, otp)).let { res ->
                    if (res.isSuccessful) Unit else error(parseError(res.errorBody()))
                }
            }
        }

    suspend fun login(email: String, pw: String): Result<SignInResponse> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = SignInRequest(userEmail = email, userPassword = pw)
                val response = api.signIn(request)

                if (response.isSuccessful) {
                    // API 호출 성공
                    val signInData = response.body()?.data ?: error("로그인 응답 데이터가 비어있습니다.")

                    // TokenManager를 사용해 토큰 및 사용자 정보 저장
                    TokenManager.saveAccessToken(signInData.accessToken ?: "")
                    TokenManager.saveRefreshToken(signInData.refreshToken ?: "")
                    TokenManager.saveUserId(signInData.userId)
                    TokenManager.saveSignInInfo(signInData) // 로그인 응답 전체 저장

                    signInData // 성공 결과 반환
                } else {
                    // API 호출 실패 (예: 400 Bad Request)
                    error(parseError(response.errorBody()))
                }
            }
        }

    suspend fun kakaoLogin(kakaoAccessToken: String): Result<SignInResponse> =
        withContext(Dispatchers.IO) {
            runCatching {
                // "Bearer " 접두사를 붙여주는 것이 일반적입니다. 백엔드 요구사항에 따라 조정하세요.
                val response = api.kakaoLogin("Bearer $kakaoAccessToken")

                if (response.isSuccessful) {
                    val signInData = response.body()?.data ?: error("카카오 로그인 응답 데이터가 비어있습니다.")

                    // 일반 로그인과 동일하게 토큰 및 사용자 정보 저장
                    TokenManager.saveAccessToken(signInData.accessToken ?: "")
                    TokenManager.saveRefreshToken(signInData.refreshToken ?: "")
                    TokenManager.saveUserId(signInData.userId)
                    TokenManager.saveSignInInfo(signInData)

                    signInData // 성공 결과 반환
                } else {
                    error(parseError(response.errorBody()))
                }
            }
        }

    suspend fun getUserInfo(): Result<UserInfoResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getUserInfo()
            if (response.isSuccessful) {
                response.body()?.data ?: error("사용자 정보가 비어있습니다.")
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }

    /** 비밀번호 변경 */
    suspend fun updatePw(body: UpdatePwRequest): Result<UpdatePwResponse> = withContext(Dispatchers.IO) {
        runCatching {
            api.updatePW(body).let { res ->
                when {
                    res.isSuccessful -> {
                        // 서버 응답에 'data' 필드가 없는 경우, 'message'를 사용해 UpdatePwResponse를 생성
                        val responseBody = res.body()
                        if (responseBody != null) {
                            UpdatePwResponse(msg = responseBody.message)
                        } else {
                            error("서버 응답이 비어 있습니다.")
                        }
                    }
                    else -> error(parseError(res.errorBody()))
                }
            }
        }
    }
}