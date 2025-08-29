package com.example.soar.DetailPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.detail.DetailRepository
import com.example.soar.Network.detail.YouthPolicyDetail
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    private val repository = DetailRepository()

    private val _policyDetail = MutableLiveData<YouthPolicyDetail>()
    val policyDetail: LiveData<YouthPolicyDetail> = _policyDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadPolicyDetail(policyId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getPolicyDetail(policyId)
                .onSuccess { detail ->
                    _policyDetail.postValue(detail)
                }
                .onFailure { e ->
                    _error.postValue(e.message ?: "정책 상세 정보를 불러오지 못했습니다.")
                }
            _isLoading.value = false
        }
    }
}