// MyPage/MypageViewModel.kt
package com.example.soar.MyPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.mypageRepository.MypageRepository
import kotlinx.coroutines.launch

class MypageViewModel : ViewModel() {
    private val repository = MypageRepository()

    private val _appliedPolicyCount = MutableLiveData<Int>()
    val appliedPolicyCount: LiveData<Int> get() = _appliedPolicyCount

    private val _commentCount = MutableLiveData<Int>()
    val commentCount: LiveData<Int> get() = _commentCount

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchUserActivityCounts() {
        viewModelScope.launch {
            repository.getAppliedPolicyCount()
                .onSuccess { count -> _appliedPolicyCount.postValue(count) }
                .onFailure { _error.postValue(it.message) }
        }
        viewModelScope.launch {
            repository.getMyCommentCount()
                .onSuccess { count -> _commentCount.postValue(count) }
                .onFailure { _error.postValue(it.message) }
        }
    }
}