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

import kotlinx.coroutines.delay

import kotlinx.coroutines.launch


data class ToastPayload(
    val message: String,
    val policyIdForUndo: String? = null // ✅ 취소 시 되돌릴 대상
)

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

    private val _toastEvent = MutableLiveData<Event<ToastPayload>>()
    val toastEvent: LiveData<Event<ToastPayload>> get() = _toastEvent

    // ✅ 0.5초 후 제거하는 지연 작업을 취소하기 위해 보관
    private val pendingRemovalJobs = mutableMapOf<String, kotlinx.coroutines.Job>()


// 외부 URL로 이동해야 할 때를 위한 LiveData

    private val _navigateToUrlEvent = MutableLiveData<Event<String>>()

    val navigateToUrlEvent: LiveData<Event<String>> get() = _navigateToUrlEvent


// ✨ 추가: 애니메이션 효과를 위해 잠시 화면에 유지할 정책 ID 집합

    private val temporarilyVisibleAppliedIds = mutableSetOf<String>()


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


// ✨ 수정: 필터링 로직 변경

    private fun applyCurrentFilter() {

        val currentPolicies = _policies.value ?: return

        val currentSelectedIds = _selectedTags.value?.map { it.tagId } ?: emptyList()


// 1. 태그로 먼저 필터링

        val tagFilteredPolicies = if (currentSelectedIds.isEmpty()) {

            currentPolicies

        } else {

            currentPolicies.filter { policy ->

                policy.tags.any { tag -> currentSelectedIds.contains(tag.tagId) }

            }

        }


// 2. 'applied' 상태로 필터링 (신청 안 한 항목만 표시)

// 단, 애니메이션을 위해 임시로 표시해야 하는 항목은 예외적으로 포함

        _filteredPolicies.value = tagFilteredPolicies.filter { policy ->

            !policy.applied || temporarilyVisibleAppliedIds.contains(policy.policyId)

        }

    }


// ✨ 수정: 체크박스 클릭 시 애니메이션 로직 추가

    fun togglePolicyApplied(policyId: String) {
        viewModelScope.launch {
            repository.togglePolicyApply(policyId)
                .onSuccess { response ->
                    // ✅ 토스트에 되돌릴 policyId를 포함(신청 완료인 경우만)
                    _toastEvent.value = Event(
                        ToastPayload(
                            message = response.message,
                            policyIdForUndo = if (response.applied) response.policyId else null
                        )
                    )

                    if (response.applied) {
                        temporarilyVisibleAppliedIds.add(response.policyId)
                    }

                    updatePolicyState(response)

                    if (response.applied) {
                        // ✅ 지연 작업을 Job으로 저장해두기 (취소 시 역전 가능)
                        val job = launch {
                            delay(500)
                            temporarilyVisibleAppliedIds.remove(response.policyId)
                            applyCurrentFilter()
                            pendingRemovalJobs.remove(response.policyId) // 정리
                        }
                        pendingRemovalJobs[response.policyId] = job
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "작업에 실패했습니다."
                }
        }
    }

    // ✅ 취소(Undo) 로직: 지연 제거 중단 + 서버/로컬 상태 복구
    fun undoApply(policyId: String) {
        // 1) 제거 지연 중이면 중단
        pendingRemovalJobs[policyId]?.cancel()
        pendingRemovalJobs.remove(policyId)

        // 2) 임시표시 목록에서 제거(필터 정상 적용 위해)
        forceLocalUnapply(policyId)  // applied=false 로컬 즉시 반영
        temporarilyVisibleAppliedIds.remove(policyId) // 필터에선 !applied라서 그대로 노출

        // 3) 서버 상태 되돌리기(토스트는 또 띄우지 않음)
        viewModelScope.launch {
            repository.togglePolicyApply(policyId)
                .onSuccess { resp ->
                    // 여기서는 "신청 취소" 토스트는 생략 (이미 취소 버튼으로 사용자가 의도 표현)
                    updatePolicyState(resp) // resp.applied == false 기대
                }
                .onFailure {
                    _error.value = it.message ?: "되돌리기에 실패했습니다."
                    // 실패 시에도 최소한 로컬은 빨간색으로 복구
                    forceLocalUnapply(policyId)
                }
        }
    }

    // ✅ 서버 되돌리기 실패 시 로컬 강제 복구(애니메이션 일관성)
    private fun forceLocalUnapply(policyId: String) {
        val currentPolicies = _policies.value?.toMutableList() ?: return
        val index = currentPolicies.indexOfFirst { it.policyId == policyId }
        if (index != -1) {
            val updated = currentPolicies[index].copy(applied = false)
            currentPolicies[index] = updated
            _policies.value = currentPolicies
            applyCurrentFilter()
        }
    }



// API 호출 성공 후, 리스트의 상태를 갱신하는 함수

    private fun updatePolicyState(toggleResponse: ToggleApplyResponse) {
        val currentPolicies = _policies.value?.toMutableList() ?: return
        val index = currentPolicies.indexOfFirst { it.policyId == toggleResponse.policyId }

        if (index != -1) {
            val oldPolicy = currentPolicies[index]
            val updatedPolicy = oldPolicy.copy(applied = toggleResponse.applied)
            currentPolicies[index] = updatedPolicy
            _policies.value = currentPolicies
            applyCurrentFilter()
        }
    }
}





