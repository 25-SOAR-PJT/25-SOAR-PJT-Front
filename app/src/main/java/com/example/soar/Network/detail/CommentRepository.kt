package com.example.soar.Network.detail

import com.example.soar.Network.ApiService
import com.example.soar.Network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject


class CommentRepository(private val api: ApiService = RetrofitClient.apiService) {

    private fun parseError(body: ResponseBody?): String =
        runCatching { JSONObject(body?.string() ?: "{}").optString("message") }
            .getOrDefault("예기치 못한 오류가 발생했습니다")

    suspend fun getComments(policyId: String): Result<List<CommentResponse>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getCommentsByPolicy(policyId)
            if (response.isSuccessful) response.body()?.data ?: emptyList()
            else error(parseError(response.errorBody()))
        }
    }

    suspend fun createComment(policyId: String, comment: String): Result<List<CommentResponse>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = CommentRequest(
                comment = comment,
                policyId = policyId
            )
            val response = api.createComment(request)
            if (response.isSuccessful) response.body()?.data ?: emptyList()
            else error(parseError(response.errorBody()))
        }
    }

    suspend fun updateComment(commentId: Long, policyId: String, newComment: String): Result<CommentResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val request = CommentRequest(
                comment = newComment,
                policyId = policyId
            )
            val response = api.updateComment(commentId, request)
            if (response.isSuccessful) response.body()?.data ?: error("응답 데이터가 없습니다.")
            else error(parseError(response.errorBody()))
        }
    }

    suspend fun deleteComment(commentId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.deleteComment(commentId)
            if (!response.isSuccessful) error(parseError(response.errorBody()))
        }
    }

    suspend fun getMyComments(): Result<List<CommentResponse>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getMyComments()
            if (response.isSuccessful) response.body()?.data ?: emptyList()
            else error(parseError(response.errorBody()))
        }
    }
}