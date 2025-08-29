package com.example.soar.Network

import com.example.soar.Network.archiving.AppliedPolicy
import com.example.soar.Network.archiving.ApplyPolicyRequest
import com.example.soar.Network.archiving.ApplyPolicyResponse
import com.example.soar.Network.archiving.BookmarkedPolicy
import com.example.soar.Network.archiving.BulkUnbookmarkRequest
import com.example.soar.Network.archiving.BulkUnbookmarkResponse
import com.example.soar.Network.archiving.ToggleApplyResponse
import com.example.soar.Network.detail.YouthPolicyDetail
import com.example.soar.Network.explore.PolicyResponse
import com.example.soar.Network.explore.RecentPoliciesRequest
import com.example.soar.Network.explore.YouthPolicy
import com.example.soar.Network.tag.TagResponse
import com.example.soar.Network.user.*
import com.example.soar.Network.tag.TagIdRequest
import okhttp3.ResponseBody
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
    suspend fun kakaoLogin(
        @Header("Authorization") kakaoAccessToken: String
    ): Response<ApiResponse<SignInResponse>> // SignInResponse를 반환한다고 가정

    @POST("/api/auth/kakao/signin")
    suspend fun kakaoSignIn(@Body request: KakaoLoginRequest): Response<ApiResponse<SignInResponse>>

    @GET(ApiConfig.User.TAG)
    suspend fun getUserTags(): Response<ApiResponse<UserTagData>>

    @POST(ApiConfig.User.TAG_MODIFY)
    suspend fun modifyUserTags(@Body request: TagIdRequest): Response<ApiResponse<UserTagData>>

    @POST(ApiConfig.User.UPDATE_PW)
    suspend fun updatePW(@Body request: UpdatePwRequest): Response<ApiResponse<UpdatePwResponse>>


    /* ───────────── Tag ───────────── */

    @GET(ApiConfig.Tag.TAG)
    suspend fun getTags(): Response<ApiResponse<List<TagResponse>>>

    /* ───────────── CurationSequence ───────────── */
    @GET(ApiConfig.Explore.MULTI_SEARCH)
    suspend fun getMultiSearchPolicies(
        @Query("keywords") keywords: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<PolicyResponse>>

    @GET(ApiConfig.UserYouthPolicyTag.QS)
    suspend fun getMultiTagSearchPolicies(
        @Query("tags") tags: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<PolicyResponse>>


    /* ───────────── Explore ───────────── */

    @GET(ApiConfig.Explore.MAIN)
    suspend fun getMainYouthPolicies(
        @Query("keyword") keyword: String? = null,
        @Query("category") category: String? = null,
        @Query("page") page: Int,
        @Query("size") size: Int = 10 // 기본 사이즈는 10으로 설정
    ): Response<ApiResponse<PolicyResponse>>

    @GET(ApiConfig.UserYouthPolicy.MAIN)
    suspend fun getMainYouthPoliciesBookmark(
        @Query("tags") tags: String? = null,
        @Query("category") category: String? = null,
        @Query("page") page: Int,
        @Query("size") size: Int = 10 // 기본 사이즈는 10으로 설정
    ): Response<ApiResponse<PolicyResponse>>

    @POST(ApiConfig.UserYouthPolicy.TOGGLE_BOOKMARK)
    suspend fun toggleBookmark(
        @Path("policyId") policyId: String
    ): Response<ApiResponse<Any>> // 응답 데이터가 없거나 메시지만 있을 경우 Any 사용

    /* ───────────── Detail ───────────── */

    @GET(ApiConfig.Details.DETAIL_BY_ID)
    suspend fun getPolicyDetail(
        @Path("policyId") policyId: String
    ): Response<ApiResponse<YouthPolicyDetail>>

    /* ───────────── Archiving ───────────── */

    @GET("/api/user-policies/bookmarks/with-meta")
    suspend fun getBookmarkedPolicies(): Response<ApiResponse<List<BookmarkedPolicy>>>

    @POST("/api/user-policies/apply/bulk")
    suspend fun applyForPolicies(
        @Body request: ApplyPolicyRequest
    ): Response<ApiResponse<ApplyPolicyResponse>>

    @POST("/api/user-policies/bookmarks/bulk-unbookmark")
    suspend fun bulkUnbookmark(
        @Body request: BulkUnbookmarkRequest
    ): Response<ApiResponse<BulkUnbookmarkResponse>>

    @POST("/api/user-policies/{policyId}/apply/toggle")
    suspend fun togglePolicyApply(
        @Path("policyId") policyId: String
    ): Response<ApiResponse<ToggleApplyResponse>>

    /* ───────────── Mypage ───────────── */

    @GET("/api/user-policies/applied")
    suspend fun getAppliedPolicies(): Response<ApiResponse<List<AppliedPolicy>>>

    @POST("/api/youth-policy/search/by-ids")
    suspend fun getPoliciesByIds(
        @Body request: RecentPoliciesRequest
    ): Response<ApiResponse<List<YouthPolicy>>>
}
