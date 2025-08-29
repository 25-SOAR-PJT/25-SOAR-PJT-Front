package com.example.soar.Network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.soar.Network.user.SignInResponse
import com.example.soar.Network.user.UserInfoResponse
import com.google.gson.Gson

object TokenManager {
    private const val PREF_NAME = "SOARPrefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_SIGN_IN_INFO = "sign_in_info"
    private const val KEY_USER_INFO = "user_info"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_IS_KAKAO_USER = "isKakaoUser"


    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveAccessToken(token: String) {
        Log.d("TokenManager", "✅ 저장 시도 accessToken = $token")
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
        Log.d("TokenManager", "✅ 저장 후 실제 값 = ${getAccessToken()}")
    }


    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }
    fun getRefreshToken(): String? =
        prefs.getString(KEY_REFRESH_TOKEN, null)

    fun saveSignInInfo(signInResponse: SignInResponse) {
        val json = Gson().toJson(signInResponse)
        prefs.edit().putString(KEY_SIGN_IN_INFO, json).apply()
    }

    fun saveUserInfo(userInfo: UserInfoResponse?) {
        if (userInfo != null) {
            val json = Gson().toJson(userInfo)
            prefs.edit()
                .putString(KEY_USER_INFO, json)
                .apply()
        }
    }



    fun getUserInfo(): UserInfoResponse? {
        val json = prefs.getString(KEY_USER_INFO, null) ?: return null
        return Gson().fromJson(json, UserInfoResponse::class.java)
    }

    fun saveUserId(id: Long) {
        prefs.edit().putLong(KEY_USER_ID, id).apply()
    }

    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1)
    }

    fun clearTokens() {
        prefs.edit().clear().apply()
    }

    fun isKakaoUser(): Boolean {
        return prefs.getBoolean(KEY_IS_KAKAO_USER, false)
    }

    // ✨ 추가: isKakaoUser 상태를 직접 저장하는 함수
    fun saveIsKakaoUser(isKakaoUser: Boolean) {
        prefs.edit().putBoolean(KEY_IS_KAKAO_USER, isKakaoUser).apply()
    }

    fun getSignInInfo(): SignInResponse? {
        val json = prefs.getString(KEY_SIGN_IN_INFO, null) ?: return null
        return try {
            Gson().fromJson(json, SignInResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

}
