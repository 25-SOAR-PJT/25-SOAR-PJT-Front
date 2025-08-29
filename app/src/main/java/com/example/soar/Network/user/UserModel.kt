package com.example.soar.Network.user

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    @SerializedName("userName") val userName: String,
    @SerializedName("userBirthDate") val userBirthDate: String, // "yyyy-MM-dd" 포맷 유지
    @SerializedName("userGender") val userGender: Boolean, // true: 남성, false: 여성
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("userPassword") val userPassword: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("agreedTerms") val agreedTerms: List<Boolean>,
    val userPhoneNumber: String,
)

data class SignUpResponse(
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("userName") val userName: String? = null,
    @SerializedName("userEmail") val userEmail: String? = null,
    @SerializedName("userGender") val userGender: Boolean? = null,
    @SerializedName("userBirthDate") val userBirthDate: String? = null,
    )

data class SignInRequest(
    @SerializedName("userEmail") val userEmail: String? = "",
    @SerializedName("userPassword") val userPassword: String
)

data class SignInResponse(
    @SerializedName("userId") val userId: Long,
    @SerializedName("userName") val userName: String? = null,
    @SerializedName("userEmail") val userEmail: String? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("kakaoAccessToken") val kakaoAccessToken: String? = null,
    @SerializedName("firstSocialLogin") val firstSocialLogin: Boolean? = null,
    @SerializedName("socialProvider") val socialProvider: String? = null
)

data class UserInfoResponse(
    @SerializedName("userId") val userId: Long,
    @SerializedName("userName") val userName: String,
    @SerializedName("userAddress") val userAddress: String?,
    @SerializedName("userBirthDate") val userBirthDate: String?,
    @SerializedName("kakaoUser") val isKakaoUser: Boolean,
)

data class UpdateUserInfoRequest(
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("userName") val userName: String?,
    @SerializedName("userBirthDate") val userBirthDate: String?,
    @SerializedName("currentPassword") val currentPassword: String?, // 추가
    @SerializedName("newPassword") val newPassword: String?,     // 추가
)

data class EmailRequest(val email: String)
data class OtpRequest(val email: String, val otp: String)
data class KakaoLoginRequest(
    @SerializedName("access_token") val accessToken: String
)

data class FindPasswordRequest(
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("inputOtp") val inputOtp: String? = null
)

data class ResetPasswordRequest(
    val userEmail: String,
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

data class Field(
    @SerializedName("fieldId") val fieldId: Int,
    @SerializedName("fieldName") val fieldName: String
)

data class UserTag(
    @SerializedName("tagId") val tagId: Int,
    @SerializedName("tagName") val tagName: String,
    @SerializedName("field") val field: Field
)

data class UserTagData(
    @SerializedName("userId") val userId: Int,
    @SerializedName("tag") val tag: List<UserTag>
)

data class UserTagResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: UserTagData
)

data class UpdatePwRequest(
    @SerializedName("newPassword") val newPassword: String? = "",
    @SerializedName("confirmPassword") val confirmPassword: String
)

data class UpdatePwResponse(
    @SerializedName("msg") val msg: String? = null
)



