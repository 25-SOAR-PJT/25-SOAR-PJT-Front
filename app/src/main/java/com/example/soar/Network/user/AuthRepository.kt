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
                val response = api.kakaoSignIn(KakaoLoginRequest(accessToken = kakaoAccessToken))
                if (response.isSuccessful) {
                    val signInData = response.body()?.data ?: error("카카오 로그인 응답 데이터가 비어있습니다.")
                    // 앱 토큰 저장
                    TokenManager.saveAccessToken(signInData.accessToken ?: "")
                    TokenManager.saveRefreshToken(signInData.refreshToken ?: "")
                    TokenManager.saveUserId(signInData.userId)
                    TokenManager.saveSignInInfo(signInData)
                    signInData
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

    suspend fun getUserDetailInfo(): Result<UserDetailInfoResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getUserDetailInfo()
            if (response.isSuccessful) {
                response.body()?.data ?: error("사용자 정보가 비어있습니다.")
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }


    suspend fun updateUserName(newName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.updateUserName(userName = newName)
            if (response.isSuccessful) {
                response.body()?.message ?: "이름이 성공적으로 변경되었습니다."
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }

    // ✨ 추가: 사용자 생년월일 변경 로직
    suspend fun updateUserBirth(newBirthDate: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = api.updateUserBirth(userBirth = newBirthDate)
                if (response.isSuccessful) {
                    response.body()?.message ?: "생년월일이 성공적으로 변경되었습니다."
                } else {
                    error(parseError(response.errorBody()))
                }
            }
        }

    // ✨ 추가: 사용자 성별 변경 로직
    // ✨ 수정: newGender 파라미터를 Nullable Boolean (Boolean?)으로 변경
    suspend fun updateUserGender(newGender: Boolean?): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = api.updateUserGender(userGender = newGender)
                if (response.isSuccessful) {
                    response.body()?.message ?: "성별이 성공적으로 변경되었습니다."
                } else {
                    error(parseError(response.errorBody()))
                }
            }
        }

    /** 비밀번호 변경 */
    suspend fun updatePw(body: UpdatePwRequest): Result<UpdatePwResponse> =
        withContext(Dispatchers.IO) {
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

    /** 아이디 찾기 */
    suspend fun findId(userName: String, userBirthdate: String): Result<FindIdResponse> =
        withContext(Dispatchers.IO) {
            runCatching {
                api.findId(userName, userBirthdate).let { response ->
                    if (response.isSuccessful) {
                        response.body()?.data ?: error("해당되는 유저 아이다가 존재하지 않습니다.")
                    } else {
                        error(parseError(response.errorBody()))
                    }
                }
            }
        }

    /** 비밀번호 찾기 */
    suspend fun findPassword(email: String, userName: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = FindPasswordRequest(userEmail = email, userName = userName)
                api.findPassword(request).let { response ->
                    if (response.isSuccessful) {
                        // 성공 시 응답 본문의 메시지를 반환
                        response.body()?.message ?: "요청을 성공적으로 처리했습니다."
                    } else {
                        // 실패 시 에러 메시지를 파싱하여 반환
                        error(parseError(response.errorBody()))
                    }
                }
            }
        }

    suspend fun deleteUser(password: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val res = api.deleteUser(mapOf("password" to password))
            if (res.isSuccessful) {
                res.body()?.message ?: "회원 탈퇴 성공"
            } else {
                error(parseError(res.errorBody()))
            }
        }
    }

    suspend fun deleteKakaoUser(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val res = api.deleteKakaoUser()
            if (res.isSuccessful) {
                res.body()?.message ?: "카카오 사용자 삭제 성공"
            } else {
                error(parseError(res.errorBody()))
            }
        }
    }

    // ✨ 오류 수정: 반환 타입 불일치 해결
    suspend fun getTermAgreement(): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            api.getTermAgreement().let { response ->
                if (response.isSuccessful) {
                    // response.body()?.data의 타입은 Boolean? 이므로, null일 경우를 처리해야 함
                    // 엘비스 연산자(?:)를 사용해 data가 null이면 예외를 발생시켜 runCatching이 실패로 처리하도록 함
                    response.body()?.data ?: error("서버에서 약관 동의 여부 데이터를 받지 못했습니다.")
                } else {
                    error(parseError(response.errorBody()))
                }
            }
        }
    }

    // ✨ 2. [선택 약관] 동의 API를 호출하는 함수 추가
    suspend fun agreeToOptionalTerm(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.agreeToTerm()
            if (response.isSuccessful) {
                response.body()?.message ?: "약관 동의가 완료되었습니다."
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }
}