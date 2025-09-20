package com.example.soar.Network.explore

import com.example.soar.Network.ApiService
import com.example.soar.Network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject

class ExploreRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    /** 공통 에러 파싱 */
    private fun parseError(body: ResponseBody?): String =
        runCatching { JSONObject(body?.string() ?: "{}").optString("message") }
            .getOrDefault("예기치 못한 오류가 발생했습니다")

    /** 정책 목록 조회 */
    suspend fun getPolicies(
        category: String?,
        tags: String?,
        page: Int
    ): Result<PolicyResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getMainYouthPoliciesBookmark(category = category, tags = tags, page = page)

            response.let { res ->
                when {
                    res.isSuccessful -> res.body()?.data
                        ?: error("서버 응답이 비어 있습니다.")
                    else -> error(parseError(res.errorBody()))
                }
            }
        }
    }

    // ✨추가: 북마크 토글 API 호출 함수✨
    suspend fun toggleBookmark(policyId: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            api.toggleBookmark(policyId).let { res ->
                when {
                    res.isSuccessful -> res.body()?.message ?: "북마크 상태가 변경되었습니다."
                    else -> error(parseError(res.errorBody()))
                }
            }
        }
    }

    /**
     * ✨추가: 다중 키워드로 정책을 검색하는 함수✨
     */
    suspend fun getMultiSearchPolicies(
        keywords: String,
        page: Int = 0,
        size: Int = 10
    ): Result<PolicyResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getMultiSearchPolicies(keywords, page, size)
            if (response.isSuccessful && response.body()?.data != null) {
                response.body()!!.data!!
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }

    suspend fun getMultiTagSearchPolicies(
        tagIds: String,
        page: Int = 0,
        size: Int = 10
    ): Result<PolicyResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getMultiTagSearchPolicies(tagIds, page, size)
            if (response.isSuccessful && response.body()?.data != null) {
                response.body()!!.data!!
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }

 
    suspend fun getPoliciesByIds(policyIds: List<String>): Result<List<YouthPolicy>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = RecentPoliciesRequest(policyIds)
            val response = api.getPoliciesByIds(request)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data!!
            } else {
                error(parseError(response.errorBody()))
            }
        }
    }


}
