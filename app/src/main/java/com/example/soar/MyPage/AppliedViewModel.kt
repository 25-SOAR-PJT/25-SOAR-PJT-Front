package com.example.soar.MyPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.archiving.AppliedPolicy
import com.example.soar.Network.archiving.ArchivingRepository
import kotlinx.coroutines.launch

class AppliedViewModel : ViewModel() {
    private val repository = ArchivingRepository()

    // 필터링된 '신청 완료' 정책 리스트
    private val _appliedPolicies = MutableLiveData<List<AppliedPolicy>>()
    val appliedPolicies: LiveData<List<AppliedPolicy>> get() = _appliedPolicies

    // 상단에 표시될 카운트 (신청 완료 수)
    private val _policyCounts = MutableLiveData<Int>()
    val policyCounts: LiveData<Int> get() = _policyCounts

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    init {
        fetchBookmarkedPolicies()
    }

    fun fetchBookmarkedPolicies() {
        if (_isLoading.value == true) return
        _isLoading.value = true

        viewModelScope.launch {
            repository.getAppliedPolicies()
                .onSuccess { allPolicies ->
                    _appliedPolicies.value = allPolicies

                    // 카운트 업데이트
                    _policyCounts.value = allPolicies.size
                }
                .onFailure {
                    _error.value = it.message ?: "데이터를 불러오는 데 실패했습니다."
                }
            _isLoading.value = false
        }
    }
}