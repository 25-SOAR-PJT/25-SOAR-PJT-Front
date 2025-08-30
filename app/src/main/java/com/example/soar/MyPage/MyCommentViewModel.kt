// MyPage/MyCommentViewModel.kt
package com.example.soar.MyPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.detail.CommentRepository
import com.example.soar.Network.detail.CommentResponse
import kotlinx.coroutines.launch

class MyCommentViewModel : ViewModel() {
    private val repository = CommentRepository()

    private val _myComments = MutableLiveData<List<CommentResponse>>()
    val myComments: LiveData<List<CommentResponse>> get() = _myComments

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchMyComments() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getMyComments()
                .onSuccess { comments ->
                    _myComments.postValue(comments)
                }
                .onFailure {
                    _error.postValue(it.message ?: "데이터를 불러오는 데 실패했습니다.")
                }
            _isLoading.value = false
        }
    }
}