// MyPage/MypageRepository.kt
package com.example.soar.Network.mypageRepository

import com.example.soar.Network.ApiService
import com.example.soar.Network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject

class MypageRepository(private val api: ApiService = RetrofitClient.apiService) {

    private fun parseError(body: ResponseBody?): String =
        runCatching { JSONObject(body?.string() ?: "{}").optString("message") }
            .getOrDefault("예기치 못한 오류가 발생했습니다")

    suspend fun getAppliedPolicyCount(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getAppliedPolicyCount()
            if (response.isSuccessful) {
                response.body()?.data ?: 0 // 데이터가 null이면 0을 반환
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }

    suspend fun getMyCommentCount(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getMyCommentCount()
            if (response.isSuccessful) {
                response.body()?.data ?: 0 // 데이터가 null이면 0을 반환
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }
}