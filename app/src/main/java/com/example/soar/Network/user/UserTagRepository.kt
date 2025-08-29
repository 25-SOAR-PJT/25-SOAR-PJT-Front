// UserTagRepository.kt

package com.example.soar.Network.user

import com.example.soar.Network.ApiService
import com.example.soar.Network.RetrofitClient
import com.example.soar.Network.tag.TagIdRequest
import com.example.soar.Network.tag.TagResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class UserTagRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    suspend fun getUserTags(): Result<List<TagResponse>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getUserTags()
            if (response.isSuccessful) {
                response.body()?.data?.tag?.map { userTag ->
                    TagResponse(
                        tagId = userTag.tagId,
                        tagName = userTag.tagName,
                        fieldId = userTag.field.fieldId
                    )
                } ?: error("사용자 태그 데이터가 비어있습니다.")
            } else {
                error("사용자 태그 불러오기 실패: ${response.code()}")
            }
        }
    }

    // ✨ 추가: 사용자 태그를 수정하는 함수
    suspend fun modifyUserTags(tagIds: List<Int>): Result<List<TagResponse>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = TagIdRequest(tagIds)
            val response = api.modifyUserTags(request)
            if (response.isSuccessful) {
                // 성공 시, API 응답에서 태그 목록을 추출하여 반환합니다.
                response.body()?.data?.tag?.map { userTag ->
                    TagResponse(
                        tagId = userTag.tagId,
                        tagName = userTag.tagName,
                        fieldId = userTag.field.fieldId
                    )
                } ?: error("수정된 태그 데이터가 비어있습니다.")
            } else {
                error("태그 수정 실패: ${response.code()}")
            }
        }
    }
}