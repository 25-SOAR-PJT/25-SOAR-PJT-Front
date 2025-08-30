// HomePage/HomeRepository.kt
package com.example.soar.Network.home

import android.util.Log
import com.example.soar.Network.ApiService
import com.example.soar.Network.RetrofitClient
import com.example.soar.Network.home.LatestPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject

class HomeRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    private fun parseError(body: ResponseBody?): String =
        runCatching { JSONObject(body?.string() ?: "{}").optString("message") }
            .getOrDefault("예기치 못한 오류가 발생했습니다")

    suspend fun getLatestPolicy(): Result<LatestPolicy?> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getLatestPolicy()
            if (response.isSuccessful) {
                // data 필드가 null일 수 있으므로 그대로 반환
                response.body()?.data
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }

    suspend fun getPopularPolicies(): Result<List<PopularPolicy>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getPopularPolicies() // 함수 이름도 복수형으로 변경
            if (response.isSuccessful) {
                Log.d("popular", response.toString())
                response.body()?.data ?: emptyList() // 빈 리스트를 기본값으로 반환
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }

    suspend fun getAgePopularPolicies(): Result<List<AgePopularPolicy>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getAgePopularPolicies()
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }

}