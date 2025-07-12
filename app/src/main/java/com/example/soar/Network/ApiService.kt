package com.example.soar.Network

import com.example.soar.Network.notice.NoticeResponseDto
import com.example.soar.Network.user.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    /* ───────────── User ───────────── */

    @POST(ApiConfig.User.SIGNIN)
    suspend fun signIn(
        @Body request: SignInRequest
    ): Response<ApiResponse<SignInResponse>>

    @POST(ApiConfig.User.SIGNUP)
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<ApiResponse<SignUpResponse>>

    @POST(ApiConfig.User.SIGNUP_OTP)
    suspend fun sendOtp(
        @Body request: EmailRequest
    ): Response<Void>

    @POST(ApiConfig.User.SIGNUP_OTP_CHECK)
    suspend fun verifyOtp(
        @Body request: OtpRequest
    ): Response<ResponseBody>


    @GET(ApiConfig.User.FIND_ID)
    suspend fun findId(
        @Query("userName") userName: String,
        @Query("userBirthdate") userBirthdate: String
    ): Response<ApiResponse<List<String>>>

    @POST(ApiConfig.User.FIND_PASSWORD)
    suspend fun findPassword(
        @Body request: FindPasswordRequest
    ): Response<ApiResponse<String>>

    @GET(ApiConfig.User.USER_INFO)
    suspend fun getUserInfo(): Response<ApiResponse<UserInfoResponse>>



    @POST(ApiConfig.User.RESET_PASSWORD)
    suspend fun resetPassword(
        @Body request: Map<String, String>
    ): Response<ApiResponse<String>>

    @POST(ApiConfig.User.KAKAO_LOGIN)
    suspend fun kakaoLogin(): Response<ApiResponse<String>>


}