package com.example.soar.ArchivingPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.archiving.ArchivingRepository
import com.example.soar.Network.archiving.BookmarkedPolicy
import com.example.soar.Network.archiving.ToggleApplyResponse
import com.example.soar.Network.tag.TagRepository
import com.example.soar.Network.tag.TagResponse
import kotlinx.coroutines.launch


class ArchivingViewModel : ViewModel() {

    private val repository = ArchivingRepository()
    private val tagRepository = TagRepository() // To get tag names from IDs

    private val _policies = MutableLiveData<List<BookmarkedPolicy>>()
    val policies: LiveData<List<BookmarkedPolicy>> get() = _policies

    private val _filteredPolicies = MutableLiveData<List<BookmarkedPolicy>>()
    val filteredPolicies: LiveData<List<BookmarkedPolicy>> get() = _filteredPolicies

    // Track selected tags
    private val _selectedTags = MutableLiveData<List<TagResponse>>()
    val selectedTags: LiveData<List<TagResponse>> get() = _selectedTags

    private var allTags: List<TagResponse> = emptyList()

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Toast 메시지와 같은 일회성 이벤트를 위한 LiveData
    private val _toastEvent = MutableLiveData<Event<String>>()
    val toastEvent: LiveData<Event<String>> get() = _toastEvent

    // 외부 URL로 이동해야 할 때를 위한 LiveData
    private val _navigateToUrlEvent = MutableLiveData<Event<String>>()
    val navigateToUrlEvent: LiveData<Event<String>> get() = _navigateToUrlEvent


    init {
        loadAllTags()
    }

    private fun loadAllTags() {
        viewModelScope.launch {
            tagRepository.getTags().onSuccess { allTags = it }
        }
    }


    fun fetchBookmarkedPolicies() {
        if (_isLoading.value == true) return
        _isLoading.value = true

        viewModelScope.launch {
            repository.getBookmarkedPolicies()
                .onSuccess { data ->
                    _policies.value = data
                    applyCurrentFilter() // 수정 코드: 현재 필터를 다시 적용
                }
                .onFailure {
                    _error.value = it.message ?: "데이터를 불러오는 데 실패했습니다."
                }
            _isLoading.value = false
        }
    }

    fun filterPoliciesByTag(selectedTagIds: List<Int>) {
        // 선택된 태그 UI를 업데이트
        _selectedTags.value = allTags.filter { it.tagId in selectedTagIds }
        // 필터 적용
        applyCurrentFilter()
    }

    private fun applyCurrentFilter() {
        val currentPolicies = _policies.value ?: return
        val currentSelectedIds = _selectedTags.value?.map { it.tagId } ?: emptyList()

        _filteredPolicies.value = if (currentSelectedIds.isEmpty()) {
            currentPolicies // 선택된 태그가 없으면 전체 목록
        } else {
            currentPolicies.filter { policy ->
                policy.tags.any { tag -> currentSelectedIds.contains(tag.tagId) }
            }
        }
    }

    // 체크박스 클릭 시 호출될 함수
    fun togglePolicyApplied(policyId: String) {
        viewModelScope.launch {
            repository.togglePolicyApply(policyId)
                .onSuccess { response ->
                    // API 응답 메시지를 Toast로 표시
                    _toastEvent.value = Event(response.message)

                    // 서버로부터 받은 최신 상태로 UI 데이터를 업데이트
                    updatePolicyState(response)

                }
                .onFailure {
                    _error.value = it.message ?: "작업에 실패했습니다."
                }
        }
    }

    // API 호출 성공 후, 리스트의 상태를 갱신하는 함수
    private fun updatePolicyState(toggleResponse: ToggleApplyResponse) {
        val currentPolicies = _policies.value?.toMutableList() ?: return
        val index = currentPolicies.indexOfFirst { it.policyId == toggleResponse.policyId }

        if (index != -1) {
            val oldPolicy = currentPolicies[index]
            // 서버에서 받은 'applied' 상태로 객체를 복사하여 갱신
            val updatedPolicy = oldPolicy.copy(applied = toggleResponse.applied)
            currentPolicies[index] = updatedPolicy
            _policies.value = currentPolicies // 원본 리스트 업데이트
            applyCurrentFilter() // 필터링된 리스트도 갱신하여 UI에 반영
        }
    }


}