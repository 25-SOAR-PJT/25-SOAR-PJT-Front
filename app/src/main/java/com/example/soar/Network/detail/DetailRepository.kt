package com.example.soar.Network.detail

import com.example.soar.Network.ApiService
import com.example.soar.Network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject

/** Common error parsing function */
private fun parseError(body: ResponseBody?): String =
    runCatching { JSONObject(body?.string() ?: "{}").optString("message") }
        .getOrDefault("예기치 못한 오류가 발생했습니다")

class DetailRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    suspend fun getPolicyDetail(policyId: String): Result<YouthPolicyDetail> = withContext(Dispatchers.IO) {
        runCatching {
            api.getPolicyDetail(policyId).let { res ->
                if (res.isSuccessful) {
                    res.body()?.data ?: error("서버 응답 데이터가 비어 있습니다.")
                } else {
                    error(parseError(res.errorBody()))
                }
            }
        }
    }

    suspend fun getPolicyStepDetail(policyId: String): Result<PolicyStepDetail> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getPolicyStepDetail(policyId)
            if (response.isSuccessful) response.body()?.data ?: error("신청 과정 정보가 없습니다.")
            else error(parseError(response.errorBody()))
        }
    }
}