// com.example.soar.ExplorePage/SearchViewModel.kt

package com.example.soar.ExplorePage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.explore.ExploreRepository
import com.example.soar.Network.explore.YouthPolicy
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val repository = ExploreRepository()

    // 검색 결과 정책 목록
    private val _policies = MutableLiveData<List<YouthPolicy>>()
    val policies: LiveData<List<YouthPolicy>> = _policies

    // 로딩 상태
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 에러 메시지
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // 페이징 관련 변수
    private var currentPage = 0
    private var isLastPage = false
    private var isDataLoading = false
    var currentQuery: String? = null // 현재 검색어 저장

    /**
     * 키워드로 정책을 검색하는 함수
     */
    fun searchPolicies(query: String?, isNewSearch: Boolean = false) {
        // 쿼리가 비어있으면 검색하지 않음
        if (query.isNullOrBlank()) return
        // 이미 로딩 중이거나 마지막 페이지인 경우 중복 호출 방지
        if (isDataLoading || (isLastPage && !isNewSearch)) return

        viewModelScope.launch {
            isDataLoading = true
            _isLoading.postValue(true)

            if (isNewSearch) {
                currentPage = 0
                isLastPage = false
                _policies.value = emptyList() // 새 검색이므로 기존 목록 초기화
                currentQuery = query
            }

            // after
            repository.getMultiSearchPolicies(
                keywords = currentQuery!!,
                page = currentPage
            ).onSuccess { response ->
                val currentList = if (isNewSearch) mutableListOf() else _policies.value?.toMutableList() ?: mutableListOf()
                currentList.addAll(response.content)
                _policies.postValue(currentList)

                isLastPage = response.isLast
                if (!isLastPage) currentPage++
            }.onFailure { e -> _error.postValue(e.message ?: "데이터를 불러오지 못했습니다.") }
            _isLoading.postValue(false)
            isDataLoading = false
        }
    }
    /**
     * ✨추가: 북마크 상태를 토글하고 API를 호출하는 함수✨
     */
    fun toggleBookmark(policy: YouthPolicy) {
        viewModelScope.launch {
            repository.toggleBookmark(policy.policyId).onSuccess { message ->
                val currentList = _policies.value?.toMutableList() ?: return@onSuccess
                val index = currentList.indexOfFirst { it.policyId == policy.policyId }

                if (index != -1) {
                    val updatedPolicy = policy.copy(bookmarked = !(policy.bookmarked ?: false))
                    currentList[index] = updatedPolicy
                    _policies.postValue(currentList)
                }

                Log.d("BookmarkToggle", message)
            }.onFailure { e ->
                _error.postValue(e.message ?: "북마크 상태 변경에 실패했습니다.")
            }
        }
    }
}