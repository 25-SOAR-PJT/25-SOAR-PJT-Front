// 파일 경로: com/example/soar/Network/tag/TagRepository.kt
package com.example.soar.Network.tag

import com.example.soar.Network.ApiService
import com.example.soar.Network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject

//// 공통 에러 파싱 함수 (AuthRepository의 것을 재사용하거나 별도 파일로 분리 가능)
//private fun parseError(body: ResponseBody?): String =
//    runCatching { JSONObject(body?.string() ?: "{}").optString("message") }
//        .getOrDefault("예기치 못한 오류가 발생했습니다")
//
//class TagRepository(
//    private val api: ApiService = RetrofitClient.apiService
//) {
//    suspend fun getTags(): Result<List<TagResponse>> = withContext(Dispatchers.IO) {
//        runCatching {
//            val response = api.getTags()
//            if (response.isSuccessful) {
//                // [수정] 중간 단계인 .tags 없이 바로 data를 사용
//                response.body()?.data ?: error("태그 데이터가 비어있습니다.")
//            } else {
//                error(parseError(response.errorBody()))
//            }
//        }
//    }
//}