package com.example.soar.Network.archiving

import com.example.soar.Network.ApiService
import com.example.soar.Network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject

class ArchivingRepository(
    private val api: ApiService = RetrofitClient.apiService
) {

    /** 공통 에러 파싱 */
    private fun parseError(body: ResponseBody?): String =
        runCatching { JSONObject(body?.string() ?: "{}").optString("message") }
            .getOrDefault("예기치 못한 오류가 발생했습니다")
    /**
     * 북마크된 정책 목록을 가져옵니다.
     */
    suspend fun getBookmarkedPolicies(): Result<List<BookmarkedPolicy>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getBookmarkedPolicies()
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }

    /**
     * 여러 정책에 대한 신청 상태를 일괄 업데이트합니다.
     */
    suspend fun applyForPolicies(policyIds: List<String>): Result<ApplyPolicyResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val request = ApplyPolicyRequest(policyIds)
            val response = api.applyForPolicies(request)
            if (response.isSuccessful) {
                response.body()?.data ?: error("서버 응답이 비어있습니다.")
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }

    /**
     * 여러 정책에 대한 북마크를 일괄 해제합니다.
     */
    suspend fun bulkUnbookmark(policyIds: List<String>): Result<BulkUnbookmarkResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val request = BulkUnbookmarkRequest(policyIds)
            val response = api.bulkUnbookmark(request)
            if (response.isSuccessful) {
                response.body()?.data ?: error("서버 응답이 비어있습니다.")
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }

    suspend fun togglePolicyApply(policyId: String): Result<ToggleApplyResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.togglePolicyApply(policyId)
            if (response.isSuccessful) {
                response.body()?.data ?: error("서버 응답 데이터가 비어있습니다.")
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }


    /**
     * 신청완료된 정책 목록을 가져옵니다.
     */
    suspend fun getAppliedPolicies(): Result<List<AppliedPolicy>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getAppliedPolicies()
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }
}