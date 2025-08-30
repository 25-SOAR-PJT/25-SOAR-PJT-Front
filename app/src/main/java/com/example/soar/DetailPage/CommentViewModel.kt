// DetailPage/CommentViewModel.kt
package com.example.soar.DetailPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.ArchivingPage.Event
import com.example.soar.Network.detail.CommentRepository
import com.example.soar.Network.detail.CommentResponse
import kotlinx.coroutines.launch

class CommentViewModel : ViewModel() {
    private val repository = CommentRepository()

    private val _comments = MutableLiveData<List<CommentResponse>>()
    val comments: LiveData<List<CommentResponse>> get() = _comments

    private val _toastEvent = MutableLiveData<Event<String>>()
    val toastEvent: LiveData<Event<String>> get() = _toastEvent

    fun loadComments(policyId: String) {
        viewModelScope.launch {
            repository.getComments(policyId)
                .onSuccess { _comments.value = it }
                .onFailure { _toastEvent.value = Event("댓글 로딩 실패: ${it.message}") }
        }
    }

    fun addComment(policyId: String, commentText: String) {
        viewModelScope.launch {
            repository.createComment(policyId, commentText)
                .onSuccess {
                    _comments.value = it // API가 반환한 최신 댓글 목록으로 업데이트
                    _toastEvent.value = Event("댓글이 등록되었습니다.")
                }
                .onFailure { _toastEvent.value = Event("댓글 등록 실패: ${it.message}") }
        }
    }

    // 파라미터 타입을 Long으로 변경
    fun editComment(commentId: Long, policyId: String, newText: String) {
        viewModelScope.launch {
            // repository 함수 호출 시 policyId 전달
            repository.updateComment(commentId, policyId, newText)
                .onSuccess { updatedComment ->
                    val list = _comments.value?.toMutableList() ?: return@onSuccess
                    val index = list.indexOfFirst { it.commentId == commentId }
                    if (index != -1) {
                        list[index] = updatedComment
                        _comments.value = list
                    }
                    _toastEvent.value = Event("댓글이 수정되었습니다.")
                }
                .onFailure { _toastEvent.value = Event("댓글 수정 실패: ${it.message}") }
        }

    }

    // 파라미터 타입을 Long으로 변경
    fun removeComment(commentId: Long) {
        viewModelScope.launch {
            repository.deleteComment(commentId)
                .onSuccess {
                    // .toString() 제거
                    _comments.value = _comments.value?.filterNot { it.commentId == commentId }
                    _toastEvent.value = Event("댓글이 삭제되었습니다.")
                }
                .onFailure { _toastEvent.value = Event("댓글 삭제 실패: ${it.message}") }
        }
    }
}