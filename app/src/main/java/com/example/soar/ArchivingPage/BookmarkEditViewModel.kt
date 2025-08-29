package com.example.soar.ArchivingPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.archiving.ArchivingRepository
import com.example.soar.Network.archiving.BookmarkedPolicy
import kotlinx.coroutines.launch

// Toast 메시지 등 일회성 이벤트를 위한 Event Wrapper
class Event<out T>(private val content: T) {
    private var hasBeenHandled = false
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}

data class EditPolicyUiModel(
    val policy: BookmarkedPolicy,
    val isSelected: Boolean = false
)

class BookmarkEditViewModel : ViewModel() {

    // 1. ArchivingRepository 인스턴스 추가
    private val repository = ArchivingRepository()

    private val _uiModels = MutableLiveData<List<EditPolicyUiModel>>()
    val uiModels: LiveData<List<EditPolicyUiModel>> get() = _uiModels

    private val _selectedCount = MutableLiveData<Int>(0)
    val selectedCount: LiveData<Int> get() = _selectedCount

    // 2. API 호출 결과를 Activity에 알리기 위한 LiveData 추가
    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> get() = _toastMessage

    fun setPolicies(policies: List<BookmarkedPolicy>) {
        _uiModels.value = policies.map { EditPolicyUiModel(it) }
    }

    fun toggleSelection(policyId: String) {
        val currentModels = _uiModels.value?.toMutableList() ?: return
        val modelIndex = currentModels.indexOfFirst { it.policy.policyId == policyId }
        if (modelIndex != -1) {
            val oldItem = currentModels[modelIndex]
            val newItem = oldItem.copy(isSelected = !oldItem.isSelected)
            currentModels[modelIndex] = newItem
            _uiModels.value = currentModels
            updateSelectedCount()
        }
    }

    fun toggleSelectAll() {
        val currentModels = _uiModels.value ?: return
        val shouldSelectAll = (selectedCount.value ?: 0) < currentModels.size
        _uiModels.value = currentModels.map { it.copy(isSelected = shouldSelectAll) }
        updateSelectedCount()
    }

    private fun updateSelectedCount() {
        _selectedCount.value = _uiModels.value?.count { it.isSelected } ?: 0
    }

    // 3. '신청완료' API를 호출하는 함수 추가
    fun applyForSelectedPolicies() {
        // 현재 선택된 정책들의 ID 목록을 가져옴
        val selectedPolicyIds = _uiModels.value
            ?.filter { it.isSelected }
            ?.map { it.policy.policyId } ?: emptyList()

        if (selectedPolicyIds.isEmpty()) {
            _toastMessage.value = Event("신청할 정책을 선택해주세요.")
            return
        }

        viewModelScope.launch {
            repository.applyForPolicies(selectedPolicyIds)
                .onSuccess { response ->
                    // API 호출 성공 시 UI 상태 업데이트
                    updatePoliciesAfterApply(selectedPolicyIds)
                    _toastMessage.value = Event("${response.appliedCount}건의 신청이 완료되었습니다.")
                }
                .onFailure {
                    _toastMessage.value = Event("오류가 발생했습니다: ${it.message}")
                }
        }
    }

    // 4. API 호출 성공 후, UI 목록을 갱신하는 함수
    private fun updatePoliciesAfterApply(appliedIds: List<String>) {
        val appliedIdSet = appliedIds.toSet()
        val currentModels = _uiModels.value ?: return

        // 성공한 항목들은 applied = true 로 상태를 변경하고, isSelected = false 로 선택 해제
        val newModels = currentModels.map { uiModel ->
            if (appliedIdSet.contains(uiModel.policy.policyId)) {
                uiModel.copy(
                    policy = uiModel.policy.copy(applied = true),
                    isSelected = false
                )
            } else {
                uiModel
            }
        }
        _uiModels.value = newModels
        updateSelectedCount() // 선택된 카운트를 0으로 다시 계산
    }

    fun deleteSelectedPolicies() {
        // 현재 선택된 정책들의 ID 목록을 가져옴
        val selectedPolicyIds = _uiModels.value
            ?.filter { it.isSelected }
            ?.map { it.policy.policyId } ?: emptyList()

        if (selectedPolicyIds.isEmpty()) {
            _toastMessage.value = Event("삭제할 정책을 선택해주세요.")
            return
        }

        viewModelScope.launch {
            repository.bulkUnbookmark(selectedPolicyIds)
                .onSuccess { response ->
                    // API 호출 성공 시 UI 목록에서 삭제된 항목들을 제거
                    removePoliciesFromUi(response.removedPolicyIds)
                    _toastMessage.value = Event("${response.removedCount}건의 북마크가 삭제되었습니다.")
                }
                .onFailure {
                    _toastMessage.value = Event("삭제 중 오류가 발생했습니다: ${it.message}")
                }
        }
    }

    // API 호출 성공 후, UI 목록에서 삭제된 정책들을 제거하는 함수
    private fun removePoliciesFromUi(removedIds: List<String>) {
        val removedIdSet = removedIds.toSet()
        val currentModels = _uiModels.value ?: return

        // 삭제되지 않은 정책들만 필터링하여 새로운 리스트를 생성
        val newModels = currentModels.filterNot { removedIdSet.contains(it.policy.policyId) }

        _uiModels.value = newModels
        updateSelectedCount() // 선택된 카운트도 다시 계산
    }
}