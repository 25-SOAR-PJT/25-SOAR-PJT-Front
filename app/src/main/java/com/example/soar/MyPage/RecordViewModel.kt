package com.example.soar.MyPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.explore.ExploreRepository
import com.example.soar.Network.explore.YouthPolicy
import kotlinx.coroutines.launch

class RecordViewModel : ViewModel() {
    private val repository = ExploreRepository()

    private val _policies = MutableLiveData<List<YouthPolicy>>()
    val policies: LiveData<List<YouthPolicy>> get() = _policies

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun loadRecentPolicies(policyIds: List<String>) {
        if (policyIds.isEmpty()) return

        _isLoading.value = true
        viewModelScope.launch {
            repository.getPoliciesByIds(policyIds)
                .onSuccess { recentPolicies ->
                    // API 응답(RecentPolicy)을 UI에서 사용할 YouthPolicy로 변환
                    val youthPolicies = recentPolicies.map {
                        YouthPolicy(
                            policyId = it.policyId,
                            policyName = it.policyName,
                            policyKeyword = it.policyKeyword,
                            largeClassification = it.largeClassification,
                            mediumClassification = it.mediumClassification,
                            supervisingInstName = it.supervisingInstName,
                            dateLabel = it.dateLabel,
                            bookmarked = it.bookmarked
                        )
                    }
                    _policies.postValue(youthPolicies)
                }
                .onFailure {
                    _error.postValue(it.message ?: "데이터를 불러오는 데 실패했습니다.")
                }
            _isLoading.value = false
        }
    }
}