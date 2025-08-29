package com.example.soar.MyPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.tag.TagResponse
import com.example.soar.Network.user.UserTagRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userTagRepository: UserTagRepository = UserTagRepository()
) : ViewModel() {
    private val _userTags = MutableLiveData<List<TagResponse>>()
    val userTags: LiveData<List<TagResponse>> get() = _userTags

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _fetchError = MutableLiveData<String>()
    val fetchError: LiveData<String> get() = _fetchError

    fun fetchUserTags() {
        if (_isLoading.value == true) return
        _isLoading.value = true
        viewModelScope.launch {
            userTagRepository.getUserTags()
                .onSuccess { tags ->
                    _userTags.value = tags
                }
                .onFailure {
                    _fetchError.value = it.message
                }
            _isLoading.value = false
        }
    }
}